package com.pjfsw.sixfiveoto;

import static java.util.stream.Collectors.toList;

import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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
    private final Map<Integer, Switch> buttons = new HashMap<>();
    private int runUntilPc = -1;

    private static final int SPI_CLOCK = 0;
    private static final int SPI_MOSI = 6;
    private static final int SPI_MISO = 7;

    private static final int GD_SELECT = 1;
    private static final int CART_SELECT = 2;

    private static final int JOY_UP = 3;
    private static final int JOY_DOWN = 4;
    private static final int JOY_LEFT = 5;
    private static final int JOY_RIGHT = 6;
    private static final int JOY_A = 7;

    private SixFiveOTo(Config properties) throws IOException, InterruptedException {
        executorService =
            Executors.newScheduledThreadPool(2);

        AddressDecoder addressDecoder = new AddressDecoder();

        clockSpeedHz = ClockspeedGetter.getClockSpeed(properties);

        List<String> parts = properties.stringPropertyNames().stream()
            .filter(p -> !p.contains("."))
            .filter(p -> !p.equals(ClockspeedGetter.CLOCKSPEED_PROPERTY))
            .collect(toList());

        Map<String, Part> collectedParts = new LinkedHashMap<>();
        for (String partName : parts) {
            Part part = PartCreator.createPart(
                properties,
                partName,
                collectedParts
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
        cpu = new Cpu(addressDecoder, registers, Collections.emptyMap() /*symbols*/);

        debugger = new Debugger(registers, Collections.emptyMap() /*symbols*/);

        // Enforce VIAs to execute directly after CPU
        for (Part part : collectedParts.values()) {
            if (part.getClockable() instanceof Via6522) {
                clockables.add(part.getClockable());
            }
        }

        screen = new Screen();

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

    /*private SixFiveOTo(byte[] prgBytes, int[] serialRomBytes, Map<Integer, String> symbols) {
        executorService =
            Executors.newScheduledThreadPool(2);

        AddressDecoder addressDecoder = new AddressDecoder();

        int programBase = ((int)prgBytes[0]&0xff) + (((int)(prgBytes[1])&0xff) << 8);
        System.out.println(String.format("- Program base: $%04X  Length: %d bytes", programBase, prgBytes.length-2));
        RomVectors romVectors = new RomVectors(programBase);
        addressDecoder.mapPeeker(romVectors, 0xFF, 0xFF);
        MemoryModule ram = MemoryModule.create32K();
        addressDecoder.mapPeeker(ram, 0x00, 0x7F);
        addressDecoder.mapPoker(ram, 0x00, 0x7F);
        MemoryModule rom = MemoryModule.create8K();
        for (int i = 0; i < prgBytes.length-2; i++) {
            rom.poke(programBase+i, prgBytes[i+2]);
        }
        addressDecoder.mapPeeker(rom, 0xF0, 0xFE);

        Via6522 via = new Via6522();
        resettables.add(via);
        clockables.add(via);
        addressDecoder.mapPoker(via, 0xD0, 0xD0);
        addressDecoder.mapPeeker(via, 0xD0, 0xD0);
        screen = new Screen();
        screen.addDrawable(new Point(Screen.W - Via6522.W ,Gameduino.H+1), via);
        addressDecoder.mapPeeker(screen, 0x80, 0x83);

        // BEGIN VIA CONFIGURATION
        addButton(via, JOY_UP, VK_W, VK_UP);
        addButton(via, JOY_DOWN, VK_S, VK_DOWN);
        addButton(via, JOY_LEFT, VK_A, VK_LEFT);
        addButton(via, JOY_RIGHT, VK_D, VK_RIGHT);
        addButton(via, JOY_A, VK_SPACE, null);

        try {

            int[] dump = readDump("src/main/resources/dumps/gddump.txt");

            Gameduino gameduino = new Gameduino(clockSpeedHz, connectSpi(via, GD_SELECT), dump);
            clockables.add(gameduino);
            resettables.add(gameduino);
            screen.addDrawable(new Point((Screen.W - Gameduino.W)/2,1), gameduino);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SerialRom cartridge = new SerialRom(connectSpi(via, CART_SELECT),  serialRomBytes);
        clockables.add(cartridge);
            screen.addDrawable(new Point((Screen.W - Gameduino.W)/2,1), gameduino);
        screen.addDrawable(new Point((Screen.W - SerialRom.W)/2, screen.getScreenHeight()-SerialRom.H-1), cartridge);

        // END OF VIA CONFIGURATION
        clockables.add(via); // TODO ugly fix because we need to sample the output of devices before next instruction

        registers = new Registers();
        cpu = new Cpu(addressDecoder, registers, symbols);

        debugger = new Debugger(registers, symbols);
        screen.addDrawable(new Point((Screen.W - Gameduino.W)/2, Gameduino.H+1), debugger);

    }*/

/*    private void addButton(Via6522 via, int pin, Integer keyCode, Integer alternateKeyCode) {
        Switch button = Switch.inverted();
        via.connectPortB(pin, button.getPin());
        buttons.put(keyCode, button);
        if (alternateKeyCode != null) {
            buttons.put(alternateKeyCode, button);
        }
    }

    private Spi connectSpi(Via6522 via, int slaveSelect) {
        Spi spi = new Spi();
        via.connectPortA(SPI_CLOCK, spi.getClock());
        via.connectPortA(SPI_MOSI, spi.getSlaveIn());
        via.connectPortA(SPI_MISO, spi.getSlaveOut());
        via.connectPortB(slaveSelect, spi.getSlaveSelect());
        return spi;
    }*/


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
            screen.increaseFrameCounter();
            frameCycleCount -= cyclesPerFrame;
        }
    }

    /**
     * Process next CPU instruction and step other clock synced circuits an equal number of cycles
     *
     * @return the number of cycles executed
     */
    private int next() {
        int cycles = cpu.next();
        if (cycles > 0) {
            for (Clockable clockable : clockables) {
                clockable.next(cycles);
            }
        }
        return cycles;
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
                System.out.println(String.format("Measured speed: %.3f MHz",
                    (double)totalCycles/(double)micros));
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
        String prgName = "";
        boolean runFullSpeed = true;

        int[] serialRomBytes = {};

        try {
            Config properties = Config.createFromFile(args[0]);
            new SixFiveOTo(properties).start(true);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }

/*
        try {
            for (Iterator<String> it = Arrays.asList(args).iterator(); it.hasNext(); ) {
                String arg = it.next();

                if (arg.equals("-step")) {
                    runFullSpeed = false;
                } else if (arg.equals("-romimage")) {
                    String romName = it.next();
                    if (romName.toLowerCase().endsWith(".asm")) {
                        romName = compileSource(romName);
                        if (romName == null) {
                            System.err.println("Terminating because of compilation failure");
                            System.exit(1);
                        }
                    }
                    if (romName.toLowerCase().endsWith(".prg")) {
                        byte[] bytes = Files.readAllBytes(new File(romName).toPath());
                        serialRomBytes = new int[bytes.length-2];
                        for (int i = 0; i < serialRomBytes.length; i++) {
                            serialRomBytes[i] = ((int)bytes[i+2]) & 0xFF;
                        }
                    }
                    System.out.println("- Attach ROM Image " + romName);
                } else if (arg.toLowerCase().endsWith(".asm")) {
                    prgName = compileSource(arg);
                    if (prgName == null) {
                        System.err.println("Terminating because of compilation failure");
                        System.exit(1);
                    }
                } else if (arg.toLowerCase().endsWith(".prg")) {
                    prgName = arg;
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        if (prgName.isEmpty()) {
            System.err.println("Specify .prg or .asm file to load");
            System.exit(1);
        }

        try {
            System.out.println("- Loading PRG file " + prgName);
            byte[] prgBytes = Files.readAllBytes(new File(prgName).toPath());
            File symbolFile = new File(prgName.replace(".prg", ".sym"));
            Map<Integer, String> symbolMap = new HashMap<>();
            if (symbolFile.isFile()) {
                System.out.println("- Loading detected symbol file " + symbolFile.getName());
                List<String> symbols =
                    Files.readAllLines(symbolFile.toPath());
                Pattern p = Pattern.compile(".label\\s*(\\w+)=\\s*(\\S+)");
                for (String symbol: symbols) {
                    Matcher m = p.matcher(symbol);
                    if (m.matches()) {

                        String s = m.group(2).substring(1);
                        int address = Integer.parseInt(s, 16);
                        symbolMap.put(address, m.group(1));
                    }
                }

                new SixFiveOTo(prgBytes, serialRomBytes, symbolMap).start(runFullSpeed);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
