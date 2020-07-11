package com.pjfsw.sixfiveoto;

import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.MemoryModule;
import com.pjfsw.sixfiveoto.addressables.Resettable;
import com.pjfsw.sixfiveoto.addressables.RomVectors;
import com.pjfsw.sixfiveoto.addressables.Screen;
import com.pjfsw.sixfiveoto.addressables.via.Via6522;
import com.pjfsw.sixfiveoto.gti.Gti;
import com.pjfsw.sixfiveoto.gti.GtiTcpTerminal;
import com.pjfsw.sixfiveoto.peripherals.Switch;
import com.pjfsw.sixfiveoto.peripherals.Led;
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

    private final int clockSpeedHz = 1_000_000;

    private final int screenRefreshRate = 60;
    private final int refreshMultiplier = 10;
    private final int refreshRate = refreshMultiplier * screenRefreshRate;
    private final int cyclesPerPeriod = clockSpeedHz/refreshRate;
    private final List<Resettable> resettables = new ArrayList<>();
    private final List<Clockable> clockables = new ArrayList<>();
    private final Map<Integer, Switch> buttons = new HashMap<>();


    private SixFiveOTo(byte[] prg, Map<Integer, String> symbols) {
        executorService =
            Executors.newScheduledThreadPool(2);

        AddressDecoder addressDecoder = new AddressDecoder();

        int programBase = ((int)prg[0]&0xff) + (((int)(prg[1])&0xff) << 8);
        System.out.println(String.format("Program base: $%04X  Length: %d bytes", programBase, prg.length-2));
        RomVectors romVectors = new RomVectors(programBase);
        addressDecoder.mapPeeker(romVectors, 0xFF, 0xFF);
        MemoryModule ram = MemoryModule.create32K();
        addressDecoder.mapPeeker(ram, 0x00, 0x7F);
        addressDecoder.mapPoker(ram, 0x00, 0x7F);
        MemoryModule rom = MemoryModule.create8K();
        for (int i = 0; i < prg.length-2; i++) {
            rom.poke(programBase+i, prg[i+2]);
        }
        addressDecoder.mapPeeker(rom, 0xF0, 0xFE);

        Via6522 via = new Via6522();
        resettables.add(via);
        clockables.add(via);
        addressDecoder.mapPoker(via, 0xD0, 0xD0);
        addressDecoder.mapPeeker(via, 0xD0, 0xD0);
        screen = new Screen();
        screen.addDrawable(new Point(320,0), via);
        addressDecoder.mapPoker(screen, 0x80, 0x83);
        addressDecoder.mapPeeker(screen, 0x80, 0x83);

        Gti gti = new Gti(80);
        resettables.add(gti);
        clockables.add(gti);
        via.setPin(0,0, gti.getClockIn()); // SPI Clock
        via.setPin(0,2, gti.getSlaveSelect()); // Slave Select
        via.setPin(0,6, gti.getSlaveIn()); // MOSI
        via.setPin(0,7, gti.getSlaveOut()); // MISO
        via.setPin(1,7, gti.getSlaveReady()); // Slave Ready
        via.setPin(1,6, gti.getConnected()); // User connected
        GtiTcpTerminal terminal = new GtiTcpTerminal(executorService,

            gti::read, gti::write, gti::setConnected);
        clockables.add(terminal);
        try {
            terminal.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*Switch aSwitch = new Switch();
        buttons.put(KeyEvent.VK_W, aSwitch);
        via.setPin(1,0, aSwitch);*/

        /**
         * RAM 0x0000 - 0x7FFF
         * Gfx 0x8000 - 0x83FF
         * ROM 0xF000 - 0xFFFF
         */
        registers = new Registers();
        cpu = new Cpu(addressDecoder, registers, symbols);

        debugger = new Debugger(registers, symbols);
        screen.addDrawable(new Point(0, 288), debugger);

    }

    private void reset() {
        cpu.reset();
        for (Resettable resettable : resettables) {
            resettable.reset();
        }
        System.out.println("RESET " + Memory.format(registers.pc));
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


    private void runFullSpeed() {
        runCount = 0;
        nanos = System.nanoTime();
        int refreshPeriod = 1000000 / refreshRate;
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
                    reset();
                    return;
                }
                cycleCount += cycles;
                updateFrameCycleCount(cycles);
            } while (cycleCount < cyclesPerPeriod);
            totalCycles += cycleCount;
            cycleCount-= cyclesPerPeriod;
        }, 0, refreshPeriod, TimeUnit.MICROSECONDS);
    }

    private void stepOne() {
        int cycles = next();
        if (cycles == 0) {
            System.out.println("CPU Crash");
            reset();
        }
        debugger.update(cpu.createDisassembler());
        updateFrameCycleCount(cycles);
    }

    private void start() {
        reset();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((event) -> {
            if (event.getID() == KeyEvent.KEY_RELEASED) {
                Switch aSwitch = buttons.get(event.getKeyCode());
                if (aSwitch != null) {
                    aSwitch.accept(false);
                    return true;
                }
            }
            if (event.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }
            switch (event.getKeyCode()) {
                case KeyEvent.VK_F8:
                    if (runner.isCancelled() || runner.isDone()) {
                        debugger.setEnabled(false);
                        runFullSpeed();
                    } else {
                        runner.cancel(false);
                        debugger.update(cpu.createDisassembler());
                        debugger.setEnabled(true);
                    }
                    break;
                case KeyEvent.VK_F6:
                    if (runner.isDone()) {
                        stepOne();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    runner.cancel(true);
                    screen.interrupt();
                    break;
                default:
                    Switch aSwitch = buttons.get(event.getKeyCode());
                    if (aSwitch != null) {
                        aSwitch.accept(true);
                        return true;
                    } else {
                        return false;
                    }
            }
            return true;
        });

        runFullSpeed();
        screen.loop();
    }

    private static String compileSource(String assembler, String source) throws InterruptedException, IOException {
        Process ps = Runtime.getRuntime().exec(ArrayUtils.addAll(assembler.split(" "), source));
        int result = ps.waitFor();
        InputStream is = ps.getInputStream();
        StringWriter writer = new StringWriter();
        String output = IOUtils.toString(is, StandardCharsets.UTF_8);
        System.out.println(output);
        if (result == 0) {
            return source.replace(".asm", ".prg");
        }
        return null;
    }

    public static void main(String[] args) {
        String prgName = "";

        for (String arg : args) {
            if (arg.toLowerCase().endsWith(".asm")) {
                try {
                    String assembler = System.getProperty("assembler");
                    if (assembler == null) {
                        System.out.println("Cannot assemble on the fly as 'assembler' property is not defined!");
                        System.out.println("Use Java property -D\"java -jar /path/to/KickAssembler/KickAss.jar\"");
                        System.out.println("Send a .PRG file as argument to launch emulator directly");
                        System.exit(1);
                    }
                    prgName = compileSource(assembler, arg);
                    if (prgName == null) {
                        System.err.println("Terminating because of compilation failure");
                        System.exit(1);
                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            } else if (arg.toLowerCase().endsWith(".prg")) {
                prgName = arg;
            }
        }

        if (prgName.isEmpty()) {
            System.err.println("Specify .prg or .asm file to load");
            System.exit(1);
        }

        try {
            byte[] bytes = Files.readAllBytes(new File(prgName).toPath());
            File symbolFile = new File(prgName.replace(".prg", ".sym"));
            Map<Integer, String> symbolMap = new HashMap<>();
            if (symbolFile.isFile()) {
                List<String> symbols =
                    Files.readAllLines(symbolFile.toPath());
                Pattern p = Pattern.compile(".label\\s*(\\w+)=\\s*(\\S+)");
                for (String symbol: symbols) {
                    Matcher m = p.matcher(symbol);
                    if (m.matches()) {

                        String s = m.group(2).substring(1);
                        System.out.println(s);
                        int address = Integer.parseInt(s, 16);
                        symbolMap.put(address, m.group(1));

                        System.out.println(String.format("Symbol: '%s'='%s'", m.group(1), m.group(2)));
                    }
                }

                new SixFiveOTo(bytes, symbolMap).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
