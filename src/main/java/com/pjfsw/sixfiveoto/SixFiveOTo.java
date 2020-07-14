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
import com.pjfsw.sixfiveoto.gameduino.Gameduino;
import com.pjfsw.sixfiveoto.instruction.Jsr;
import com.pjfsw.sixfiveoto.peripherals.Switch;
import com.pjfsw.sixfiveoto.registers.Registers;
import com.pjfsw.sixfiveoto.spi.Spi;

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

    private final int clockSpeedHz = 2_500_000;

    private final int screenRefreshRate = 60;
    private final int refreshMultiplier = 10;
    private final int refreshRate = refreshMultiplier * screenRefreshRate;
    private final int cyclesPerPeriod = clockSpeedHz/refreshRate;
    private final List<Resettable> resettables = new ArrayList<>();
    private final List<Clockable> clockables = new ArrayList<>();
    private final Map<Integer, Switch> buttons = new HashMap<>();
    private int runUntilPc = -1;


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
        screen.addDrawable(new Point(Screen.W - Via6522.W ,Gameduino.H+1), via);
        addressDecoder.mapPoker(screen, 0x80, 0x83);
        addressDecoder.mapPeeker(screen, 0x80, 0x83);

        try {

            int[] dump = readDump("src/main/resources/dumps/gddump.txt");

            Spi spi = new Spi();
            via.setPin(0,0, spi.getClock());
            via.setPin(0,1, spi.getSlaveSelect());
            via.setPin(0,6, spi.getSlaveIn());
            via.setPin(0,7, spi.getSlaveOut());

            Gameduino gameduino = new Gameduino(spi, dump);
            clockables.add(gameduino);
            screen.addDrawable(new Point((Screen.W - Gameduino.W)/2,1), gameduino);
        } catch (IOException e) {
            e.printStackTrace();
        }

        clockables.add(via); // TODO ugly fix because we need to sample the output of devices before next instruction


//        Gti gti = new Gti(80);
//        resettables.add(gti);
//        clockables.add(gti);
//        via.setPin(0,0, gti.getClockIn()); // SPI Clock
//        via.setPin(0,2, gti.getSlaveSelect()); // Slave Select
//        via.setPin(0,6, gti.getSlaveIn()); // MOSI
//        via.setPin(0,7, gti.getSlaveOut()); // MISO
//        via.setPin(1,7, gti.getSlaveReady()); // Slave Ready
//        via.setPin(1,6, gti.getConnected()); // User connected
//        GtiTcpTerminal terminal = new GtiTcpTerminal(executorService,
//
//            gti::read, gti::write, gti::setConnected);
//        clockables.add(terminal);
//        try {
//            terminal.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

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
        screen.addDrawable(new Point((Screen.W - Gameduino.W)/2, Gameduino.H+1), debugger);

    }

    private static int[] readDump(String fileName) throws IOException {
        List<Integer> dump = new ArrayList<Integer>();
        List<String> lines = Files.readAllLines(
            new File(fileName).toPath());
        for (String line : lines) {
            for (String number : line.split(",")) {
                dump.add(Integer.parseInt(number.trim(), 16));
            }
        }

        return dump.stream().mapToInt(i->i).toArray();


    }

    private void reset() {
        cpu.reset();
        for (Resettable resettable : resettables) {
            resettable.reset();
        }
        if (runner == null || runner.isCancelled() || runner.isDone()) {
            debugger.update(cpu.createDisassembler());
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

    private void stepMode() {
        if (runner != null) {
            runner.cancel(false);
        }
        debugger.update(cpu.createDisassembler());
        debugger.setEnabled(true);
    }

    private void runFullSpeed() {
        debugger.setEnabled(false);

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
            reset();
        }
        debugger.update(cpu.createDisassembler());
        updateFrameCycleCount(cycles);
    }

    private void start(boolean fullSpeed) {
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
                case KeyEvent.VK_END:
                    reset();
                    break;

                case KeyEvent.VK_F8:
                    if (runner == null || runner.isCancelled() || runner.isDone()) {
                        runUntilPc = -1;
                        runFullSpeed();
                    } else {
                        stepMode();
                    }
                    break;
                case KeyEvent.VK_F5:
                    if (runner == null || runner.isDone()) {
                        stepOne();
                    }
                    break;
                case KeyEvent.VK_F6:
                    if (runner == null || runner.isDone()) {
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
                    if (runner != null) {
                        runner.cancel(true);
                    }
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
        if (fullSpeed) {
            runFullSpeed();
        } else {
            stepMode();
        }
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
        boolean runFullSpeed = true;

        for (String arg : args) {
            if (arg.equals("-step")) {
                runFullSpeed = false;
            }
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

                new SixFiveOTo(bytes, symbolMap).start(runFullSpeed);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
