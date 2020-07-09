package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum Bit implements Instruction {
    BITZ(AddressingMode.ZEROPAGE, 0x24, "BIT", 3),
    BITA(AddressingMode.ABSOLUTE, 0x2C, "BIT", 4),
    // 65C02 instructions
    BITI(AddressingMode.IMMEDIATE, 0x89, "BIT", 2),
    BITZX(AddressingMode.ZEROPAGE_INDEXED_X, 0x34, "BIT", 4),
    BITAX(AddressingMode.INDEXED_X, 0x3C, "BIT", 4)
    ;

    private final AddressingMode addressingMode;
    private final int opcode;
    private final String mnemonic;
    private final int cycles;

    Bit(AddressingMode addressingMode, int opcode, String mnemonic, int cycles) {
        this.addressingMode = addressingMode;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.cycles = cycles;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int address = addressingMode.getEffectiveAddress(registers, peeker);
        int value = peeker.peek(address);
        registers.and(registers.a(), value);
        if (AddressingMode.IMMEDIATE != addressingMode) {
            registers.n = (value & 0x80) != 0;
            registers.v = (value & 0x40) != 0;
        }
        registers.incrementPc(addressingMode.getParameterSize());
        return cycles;
    }


    @Override
    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public MnemonicFormatter getMnemonicFormatter() {
        return addressingMode.getFormatter();
    }
}
