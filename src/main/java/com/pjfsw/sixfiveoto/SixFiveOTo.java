package com.pjfsw.sixfiveoto;

import static java.util.stream.Collectors.toList;

import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Interrupt;
import com.pjfsw.sixfiveoto.addressables.Resettable;
import com.pjfsw.sixfiveoto.addressables.Screen;
import com.pjfsw.sixfiveoto.addressables.via.Via6522;
import com.pjfsw.sixfiveoto.gameduino.Gameduino;
import com.pjfsw.sixfiveoto.instruction.Jsr;
import com.pjfsw.sixfiveoto.peripherals.Switch;
import com.pjfsw.sixfiveoto.registers.Registers;

public class SixFiveOTo {
    private final Cpu cpu;
    private final Registers registers;
    private final Screen screen;
    private final Debugger debugger;
    private final CpuStatistics cpuStatistics;
    private ScheduledFuture<?> runner;
    private long runCount;
    private long nanos;
    private long totalCycles;
    long cycleCount = 0;
    private final ScheduledExecutorService executorService;
    private int frameCycleCount;

    //private final int clockSpeedHz = 2_560_000;
    private final int clockSpeedHz;

    private final int screenRefreshRate = 60;
    private final int refreshMultiplier = 10;
    private final int refreshRate = refreshMultiplier * screenRefreshRate;
    private final List<Resettable> resettables = new ArrayList<>();
    private final List<Clockable> clockables = new ArrayList<>();
    private final List<Interrupt> interrupts = new ArrayList<>();
    private final Map<Integer, Switch> buttons = new HashMap<>();
    private int runUntilPc = -1;

    private SixFiveOTo(Config properties)
        throws IOException, InterruptedException
    {
        executorService =
            Executors.newScheduledThreadPool(2);

        this.cpuStatistics = new CpuStatistics();
        AddressDecoder addressDecoder = new AddressDecoder();

        clockSpeedHz = ClockspeedGetter.getClockSpeed(properties);

        List<String> parts = properties.stringPropertyNames().stream()
            .filter(p -> !p.contains("."))
            .filter(p -> !p.equals(ClockspeedGetter.CLOCKSPEED_PROPERTY))
            .collect(toList());

        Map<Integer, String> symbols = new HashMap<>();
        Map<String, Part> collectedParts = new LinkedHashMap<>();
        for (String partName : parts) {
            Part part = PartCreator.createPart(
                properties,
                partName,
                collectedParts,
                symbols
            );
            PageRange range = PageRange.createFromProperty(properties, partName);
            if (range != null) {
                if (part.getPeeker() != null) {
                    addressDecoder.mapPeeker(part.getPeeker(), range.getStart(), range.getEnd());
                }
                if (part.getPoker() != null) {
                    addressDecoder.mapPoker(part.getPoker(), range.getStart(), range.getEnd());
                }
            }
            collectedParts.put(partName, part);
        }

        registers = new Registers();
        cpu = new Cpu(addressDecoder, registers, symbols, cpuStatistics);

        debugger = new Debugger(registers, symbols);

        // Enforce VIAs to execute directly after CPU
        for (Part part : collectedParts.values()) {
            if (part.getClockable() instanceof Via6522) {
                clockables.add(part.getClockable());
            }
        }

        screen = new Screen(cpuStatistics);

        // Add  the reset normally
        for (Entry<String, Part> entry : collectedParts.entrySet()) {
            String name = entry.getKey();
            Part part = entry.getValue();
            if (part.getDrawable() != null) {
                int x = Integer.parseInt(properties.getProperty(name + ".x", "0"));
                int y = Integer.parseInt(properties.getProperty(name + ".y", "0"));
                if (y < 0) {
                    y = screen.getScreenHeight() - Math.abs(y);
                }
                screen.addDrawable(new Point(x,y), part.getDrawable());
            }
            if (part.getResettable() != null) {
                resettables.add(part.getResettable());
            }
            // Don't add VIAs randomly in the middle
            if (part.getClockable() != null && !(part.getClockable() instanceof Via6522)) {
                clockables.add(part.getClockable());
            }
            if (part.getSwitch() != null) {
                int keycode = Integer.parseInt(properties.getProperty(name + ".keycode", "FF"), 16);
                buttons.put(keycode, part.getSwitch());
            }
            if (part.getInterrupt() != null) {
                interrupts.add(part.getInterrupt());
            }
        }

        // Enforce VIAs to execute once again last
        for (Part part : collectedParts.values()) {
            if (part.getClockable() instanceof Via6522) {
                clockables.add(part.getClockable());
            }
        }

        screen.addDrawable(new Point((Screen.W - Gameduino.W)/2, Gameduino.H+1), debugger);
        // Map keys
    }

    private void stop() {
        if (runner != null) {
            runner.cancel(false);
        }
    }

    private boolean isRunning() {
        return runner != null && !runner.isCancelled() && !runner.isDone();
    }

    private static void wait(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void reset(boolean hardReset) {
        boolean runAgain = isRunning();
        System.out.println("- Triggering RESET " + Memory.format(registers.pc));
        if (runAgain) {
            stop();
            wait(50);
        }
        cpu.reset();
        for (Resettable resettable : resettables) {
            resettable.reset(hardReset);
        }
        if (!runAgain) {
            debugger.update(cpu.createDisassembler());
        }
        if (runAgain) {
            runFullSpeed();
        }

    }
    private void updateFrameCycleCount(int cycles) {
        frameCycleCount += cycles;
        int cyclesPerFrame = clockSpeedHz / screenRefreshRate;
        if (frameCycleCount >= cyclesPerFrame) {
            frameCycleCount -= cyclesPerFrame;
        }
    }

    /**
     * Process next CPU instruction and step other clock synced circuits an equal number of cycles
     *
     * @return the number of cycles executed
     */
    private int next() {
        boolean irq = false;
        for (Interrupt interrupt : interrupts) {
            irq |= interrupt.hasIrq();
        }
        int cycles = 0;
        if (irq) {
            int irqCycles = cpu.irq();
            if (irqCycles > 0) {
                clock(irqCycles);
                cycles += irqCycles;
            }
        }
        int cpuCycles = cpu.next();
        if (cpuCycles > 0) {
            clock(cpuCycles);
            cycles += cpuCycles;
        }
        return cycles;
    }

    private void clock(int cycles) {
        for (Clockable clockable : clockables) {
            clockable.next(cycles);
        }
    }
    private void stepMode() {
        stop();
        debugger.update(cpu.createDisassembler());
        debugger.setEnabled(true);
    }

    private void runFullSpeed() {
        debugger.setEnabled(false);

        frameCycleCount = 0;
        runCount = 0;
        cycleCount = 0;
        totalCycles = 0;
        nanos = System.nanoTime();
        int refreshPeriod = 1000000 / refreshRate;
        int cyclesPerPeriod = clockSpeedHz/refreshRate;
        runner = executorService.scheduleAtFixedRate(() -> {
            runCount++;
            if (runCount % refreshRate == 0) {
                double micros = (System.nanoTime() - nanos)/1000.0;
                cpuStatistics.setSpeed((double)totalCycles/micros);
                nanos = System.nanoTime();

                totalCycles = 0;

            }
            do {
                int cycles = next();
                if (cycles == 0) {
                    reset(false);
                    return;
                }
                cycleCount += cycles;
                updateFrameCycleCount(cycles);
            } while (runUntilPc != registers.pc && cycleCount < cyclesPerPeriod);
            if (runUntilPc == registers.pc) {
                stepMode();
            }
            totalCycles += cycleCount;
            cycleCount-= cyclesPerPeriod;
        }, 0, refreshPeriod, TimeUnit.MICROSECONDS);
    }

    private void stepOne() {
        int cycles = next();
        if (cycles == 0) {
            System.out.println("CPU Crash");
            reset(false);
        }
        debugger.update(cpu.createDisassembler());
        updateFrameCycleCount(cycles);
    }

    private void start(boolean fullSpeed) {
        reset(true);

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((event) -> {
            if (event.getID() == KeyEvent.KEY_RELEASED) {
                Switch aSwitch = buttons.get(event.getKeyCode());
                if (aSwitch != null) {
                    aSwitch.setState(false);
                    return true;
                }
            }
            if (event.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }
            switch (event.getKeyCode()) {
                case KeyEvent.VK_END:
                    reset(event.isShiftDown());
                    break;

                case KeyEvent.VK_F8:
                    if (!isRunning()) {
                        runUntilPc = -1;
                        runFullSpeed();
                    } else {
                        stepMode();
                    }
                    break;
                case KeyEvent.VK_F5:
                    if (!isRunning()) {
                        stepOne();
                    }
                    break;
                case KeyEvent.VK_F6:
                    if (!isRunning()) {
                        if (cpu.getNextOpcode() == Jsr.OPCODE) {
                            Disassembler disassembler = cpu.createDisassembler();
                            disassembler.disassemble();
                            runUntilPc = disassembler.getPc();
                            runFullSpeed();
                        } else {
                            stepOne();
                        }
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    stop();
                    break;
                default:
                    Switch aSwitch = buttons.get(event.getKeyCode());
                    if (aSwitch != null) {
                        aSwitch.setState(true);
                        return true;
                    } else {
                        return false;
                    }
            }
            return true;
        });
        if (fullSpeed) {
            runFullSpeed();
        } else {
            stepMode();
        }
        screen.loop();
    }


    public static void main(String[] args) {
         try {
            Config properties = Config.createFromFile(args[0]);
            new SixFiveOTo(properties).start(true);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
