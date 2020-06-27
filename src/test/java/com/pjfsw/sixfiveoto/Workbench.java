package com.pjfsw.sixfiveoto;

import static java.util.Collections.emptyList;

import java.util.List;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.addressables.RomVectors;
import com.pjfsw.sixfiveoto.registers.Registers;

/**
 * A simple workbench computer with the following memory layout:
 *
 * 0x0000-0x03FF 1KB RAM
 * 0xF000-0xF3FF 1KB ROM code
 *
 */
public class Workbench implements Peeker, Poker {
    private static final int CODEPAGE = 0xF0;
    public static final int CODEBASE = CODEPAGE << 8;
    private final Cpu cpu;
    private final Registers registers;
    private final AddressDecoder addressDecoder;


    public Workbench(List<Integer> byteCode) {
        this.registers = new Registers();
        addressDecoder = new AddressDecoder();
        addressDecoder.mapPeeker(new Memory1K(byteCode), CODEPAGE, CODEPAGE+3);
        addressDecoder.mapPeeker(new RomVectors(CODEBASE), 0xFF, 0xFF);
        Memory1K ram = new Memory1K(emptyList());
        addressDecoder.mapPeeker(ram, 0,3);
        addressDecoder.mapPoker(ram, 0,3);

        cpu = new Cpu(addressDecoder, registers);
    }

    public Cpu cpu() {
        return cpu;
    }

    public long cycles() {
        return cpu.getCycles();
    }

    public int run(int instructions) {
        int cycles = 0;
        for (int i = 0; i < instructions; i++) {
            cycles += cpu.next();
        }
        return cycles;
    }

    @Override
    public int peek(final int address) {
        return addressDecoder.peek(address);
    }

    @Override
    public void poke(final int address, final int data) {
        addressDecoder.poke(address, data);
    }

    public Registers registers() {
        return registers;
    }

    public static class Memory1K implements Peeker, Poker {
        private final int[] byteCode;

        public Memory1K(List<Integer> byteCode) {
            this.byteCode = new int[1024];
            for (int i = 0; i < byteCode.size(); i++) {
                this.byteCode[i] = byteCode.get(i);
            }
        }


        @Override
        public int peek(int address) {
            return byteCode[address & 0x03FF];
        }

        @Override
        public void poke(final int address, final int data) {
            byteCode[address & 0x03FF] = Word.lo(data);
        }
    }


}
