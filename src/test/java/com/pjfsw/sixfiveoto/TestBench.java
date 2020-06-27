package com.pjfsw.sixfiveoto;

import java.util.Collection;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class TestBench implements Peeker, Poker {
    private static final int CODEPAGE = 0xF0;
    private final Cpu cpu;
    private final Registers registers;
    private final AddressDecoder addressDecoder;

    public TestBench(Collection<Integer> byteCode) {
        this.registers = new Registers();
        addressDecoder = new AddressDecoder();
        addressDecoder.mapPeeker(new RamPage(byteCode), CODEPAGE, CODEPAGE);
        addressDecoder.mapPeeker(new RomPage(CODEPAGE << 8), 0xFF, 0xFF);
        cpu = new Cpu(addressDecoder, registers);
    }

    public Cpu cpu() {
        return cpu;
    }

    public long cycles() {
        return cpu.getCycles();
    }

    public void run(int instructions) {
        for (int i = 0; i < instructions; i++) {
            cpu.next();
        }
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

    private static class RomPage implements Peeker {
        private final int bootVector;

        public RomPage(int bootVector) {
            this.bootVector = bootVector;
        }

        @Override
        public int peek(final int address) {
            if (Word.lo(address) == 0xFC) {
                return Word.lo(bootVector);
            } else if (Word.lo(address) == 0xFD) {
                return Word.hi(bootVector);
            } else {
                return 0;
            }
        }
    }

    private static class RamPage implements Peeker, Poker {
        private final Integer[] byteCode;

        public RamPage(Collection<Integer> byteCode) {
            this.byteCode = byteCode.toArray(new Integer[0]);
        }


        @Override
        public int peek(int address) {
            if (Word.lo(address) < byteCode.length) {
                return byteCode[Word.lo(address)];
            } else {
                return 0;
            }
        }

        @Override
        public void poke(final int address, final int data) {
            if (Word.lo(address) < byteCode.length) {
                byteCode[Word.lo(address)] = Word.lo(data);
            }
        }
    }


}
