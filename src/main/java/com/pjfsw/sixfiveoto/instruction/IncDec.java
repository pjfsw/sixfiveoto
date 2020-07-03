package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum IncDec implements Instruction {
    DEC(AddressingMode.ABSOLUTE, -1, 0xCE, "DEC $%04X", 6),
    DECX(AddressingMode.INDEXED_X, -1, 0xDE, "DEC $%04X,X", 7),
    DECZ(AddressingMode.ZEROPAGE, -1, 0xC6, "DEC $%02X", 5),
    DECZX(AddressingMode.ZEROPAGE_INDEXED_X, -1, 0xD6, "DEC $%02X,X", 6),
    INC(AddressingMode.ABSOLUTE, 1, 0xEE, "INC $%04X", 6),
    INCX(AddressingMode.INDEXED_X, 1, 0xFE, "INC $%04X,X", 7),
    INCZ(AddressingMode.ZEROPAGE, 1, 0xE6, "INC $%02X", 5),
    INCZX(AddressingMode.ZEROPAGE_INDEXED_X, 1, 0xF6, "INC $%02X,X", 6);

    private final AddressingMode addressingMode;
    private final int cycles;
    private final int direction;
    private final int opcode;
    private final String mnemonic;

    IncDec(AddressingMode addressingMode, int direction, int opcode, String mnemonic, int cycles) {
        this.addressingMode = addressingMode;
        this.direction = direction;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
        this.cycles = cycles;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, Peeker peeker, Poker poker) {
        int address = addressingMode.getEffectiveAddress(registers, peeker);
        poker.poke(address, registers.add(peeker.peek(address), direction));
        registers.incrementPc(addressingMode.getParameterSize());
        return cycles;
    }

    @Override
    public String getMnemonic(final Integer word) {
        return String.format(mnemonic, word);
    }
}