package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum IncDec implements Instruction {
    DEC(AddressingMode.ABSOLUTE, -1, 0xCE, 6),
    DECX(AddressingMode.INDEXED_X, -1, 0xDE, 7),
    DECZ(AddressingMode.ZEROPAGE, -1, 0xC6, 5),
    DECZX(AddressingMode.ZEROPAGE_INDEXED_X, -1, 0xD6, 6),
    INC(AddressingMode.ABSOLUTE, 1, 0xEE, 6),
    INCX(AddressingMode.INDEXED_X, 1, 0xFE,  7),
    INCZ(AddressingMode.ZEROPAGE, 1, 0xE6, 5),
    INCZX(AddressingMode.ZEROPAGE_INDEXED_X, 1, 0xF6, 6);

    private final AddressingMode addressingMode;
    private final int cycles;
    private final int direction;
    private final int opcode;

    IncDec(AddressingMode addressingMode, int direction, int opcode, int cycles) {
        this.addressingMode = addressingMode;
        this.direction = direction;
        this.opcode = opcode;
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
    public String getMnemonic() {
        return direction == 1 ? "INC" : "DEC";
    }

    @Override
    public MnemonicFormatter getMnemonicFormatter() {
        return addressingMode.getFormatter();
    }

    @Override
    public int length() {
        return addressingMode.getParameterSize()+1;
    }

}
