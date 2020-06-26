package com.pjfsw.sixfiveoto;

import com.pjfsw.sixfiveoto.addressables.TestRom;
import com.pjfsw.sixfiveoto.registers.Registers;

public class SixFiveOTo {
    private final Cpu cpu;
    private final Registers registers;

    private SixFiveOTo() {
        AddressDecoder addressDecoder = new AddressDecoder();
        addressDecoder.mapFunction(new TestRom(0xF000), 0xF0, 0xFF);

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
        new SixFiveOTo().start();
    }

}
