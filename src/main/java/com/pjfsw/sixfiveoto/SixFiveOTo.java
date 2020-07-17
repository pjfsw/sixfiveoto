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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
import com.pjfsw.sixfiveoto.serialrom.SerialRom;
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

    private final int clockSpeedHz = 2_560_000;

    private final int screenRefreshRate = 60;
    private final int refreshMultiplier = 10;
    private final int refreshRate = refreshMultiplier * screenRefreshRate;
    private final int cyclesPerPeriod = clockSpeedHz/refreshRate;
    private final List<Resettable> resettables = new ArrayList<>();
    private final List<Clockable> clockables = new ArrayList<>();
    private final Map<Integer, Switch> buttons = new HashMap<>();
    private int runUntilPc = -1;


    private SixFiveOTo(byte[] prgBytes, int[] serialRomBytes, Map<Integer, String> symbols) {
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
        addressDecoder.mapPoker(screen, 0x80, 0x83);
        addressDecoder.mapPeeker(screen, 0x80, 0x83);

        try {

            int[] dump = readDump("src/main/resources/dumps/gddump.txt");

            Spi spi = new Spi();
            via.connectPortA(0, spi.getClock());
            via.connectPortA(6, spi.getSlaveIn());
            via.connectPortA(7, spi.getSlaveOut());

            via.connectPortB(1, spi.getSlaveSelect());

            Gameduino gameduino = new Gameduino(clockSpeedHz, spi, dump);
            clockables.add(gameduino);
            resettables.add(gameduino);
            screen.addDrawable(new Point((Screen.W - Gameduino.W)/2,1), gameduino);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Spi spi = new Spi();
        via.connectPortA(0, spi.getClock());
        via.connectPortA(6, spi.getSlaveIn());
        via.connectPortA(7, spi.getSlaveOut());
        via.connectPortB(2, spi.getSlaveSelect());

        SerialRom cartridge = new SerialRom(spi,  serialRomBytes);
        clockables.add(cartridge);
        screen.addDrawable(new Point((Screen.W - SerialRom.W)/2, screen.getScreenHeight()-SerialRom.H-1), cartridge);

        clockables.add(via); // TODO ugly fix because we need to sample the output of devices before next instruction

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
                    aSwitch.accept(false);
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

    private static String compileSource(String source) throws InterruptedException, IOException {
        String assembler = System.getProperty("assembler");
        if (assembler == null) {
            System.out.println("Cannot assemble on the fly as 'assembler' property is not defined!");
            System.out.println("Use Java property -D\"java -jar /path/to/KickAssembler/KickAss.jar\"");
            System.out.println("Send a .PRG file as argument to launch emulator directly");
            System.exit(1);
        }

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

        int[] serialRomBytes = {};

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
        }
    }
}
