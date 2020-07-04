package com.pjfsw.sixfiveoto;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.pjfsw.sixfiveoto.addressables.MemoryModule;
import com.pjfsw.sixfiveoto.addressables.RomVectors;
import com.pjfsw.sixfiveoto.addressables.Screen;
import com.pjfsw.sixfiveoto.registers.Registers;

public class SixFiveOTo {
    private final Cpu cpu;
    private final Registers registers;
    private final Screen screen;
    private ScheduledFuture<?> runner;
    private long crashCount;
    private long runCount;
    private long nanos;
    private long totalCycles;
    long cycleCount = 0;
    private ScheduledExecutorService executorService;
    private int frameCycleCount;

    private final int clockSpeedHz = 2500000;

    private final int screenRefreshRate = 60;
    private final int refreshMultiplier = 10;
    private final int refreshRate = refreshMultiplier * screenRefreshRate;
    private final int cyclesPerPeriod = clockSpeedHz/refreshRate;

    private SixFiveOTo(byte[] prg) {

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

        screen = new Screen(512,512);
        addressDecoder.mapPoker(screen, 0x80, 0x83);
        addressDecoder.mapPeeker(screen, 0x80, 0x83);

        /**
         * RAM 0x0000 - 0x7FFF
         * Gfx 0x8000 - 0x83FF
         * ROM 0xF000 - 0xFFFF
         */
        registers = new Registers();
        cpu = new Cpu(addressDecoder, registers);
    }

    private void reset() {
        cpu.reset();
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
                int cycles = cpu.next();
                if (cycles == 0) {
                    System.out.println(cpu.toString());
                    crashCount++;
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
        StringBuilder sb = new StringBuilder();
        sb.append(Memory.format(registers.pc));
        sb.append("  ");
        sb.append(cpu.toString());
        System.out.println(sb.toString());
        int cycles = cpu.next();
        if (cycles == 0) {
            System.out.println("CPU Crash");
            reset();
        }
        updateFrameCycleCount(cycles);
    }

    private void start() {
        reset();
        executorService =
            Executors.newSingleThreadScheduledExecutor();

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((event) -> {
            if (event.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }
            switch (event.getKeyCode()) {
                case KeyEvent.VK_F8:
                    if (runner.isCancelled() || runner.isDone()) {
                        runFullSpeed();
                    } else {
                        runner.cancel(false);
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
            }
            return true;
        });

        runFullSpeed();
        screen.loop();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Specify .PRG file to load");
            System.exit(1);
        }

        String filename = args[0];
        try {
            byte[] bytes = Files.readAllBytes(new File(filename).toPath());
            new SixFiveOTo(bytes).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
