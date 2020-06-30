package com.pjfsw.sixfiveoto.instruction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdIndirectIndexed implements Instruction {
    ANDY(Operation.AND, 0x31, "AND ($%02X),Y"),
    EORY(Operation.EOR, 0x51, "EOR ($%02X),Y"),
    LDAY(Operation.EQU, 0xB1, "LDA ($%02X),Y"),
    ORAY(Operation.ORA, 0x11, "ORA ($%02X),Y");

    private final int opcode;
    private final String mnemonic;
    private final Operation operation;

    LdIndirectIndexed(
        Operation operation, int opcode, String mnemonic) {
        this.operation = operation;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int address = AddressingMode.INDIRECT_INDEXED.getEffectiveAddress(registers, peeker);
        registers.a(operation.apply(registers,peeker.peek(address)));
        int cycles = 5 + AddressingMode.INDIRECT_INDEXED.getPenalty(registers, peeker);;
        registers.incrementPc(1);
        return cycles;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }
}
