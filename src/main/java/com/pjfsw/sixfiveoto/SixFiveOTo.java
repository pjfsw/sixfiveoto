package com.pjfsw.sixfiveoto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import com.pjfsw.sixfiveoto.addressables.MemoryModule;
import com.pjfsw.sixfiveoto.addressables.RomVectors;
import com.pjfsw.sixfiveoto.registers.Registers;

public class SixFiveOTo {
    private final Cpu cpu;
    private final Registers registers;

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

        /**
         * RAM 0x0000 - 0x7FFF
         * ROM 0xF000 - 0xFFFF
         */
        registers = new Registers();
        cpu = new Cpu(addressDecoder, registers);
    }

    private void reset() {
        cpu.reset();
        System.out.println("RESET " + Memory.format(registers.pc));
    }
    private void start() {
        reset();
        int cycleCount = 0;
        while (true) {
            StringBuilder sb = new StringBuilder();

            //sb.append(String.format("%08X", cycleCount));
            //sb.append("  ");
            sb.append(Memory.format(registers.pc));
            sb.append("  ");
            sb.append(cpu.toString());
            System.out.println(sb.toString());
            int cycles = cpu.next();
            cycleCount += cycles;
            if (cycles == 0) {
                System.out.println("CPU Crash");
                reset();
                cycleCount = 0;
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


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
