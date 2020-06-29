package com.pjfsw.sixfiveoto.instruction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdIndexedIndirect implements Instruction {
    ANDX(Operation.AND, 0x21, "AND ($%02X,X)"),
    EORX(Operation.EOR, 0x41, "EOR ($%02X,X)"),
    LDAX(Operation.EQU, 0xA1, "LDA ($%02X,X)"),
    ORAX(Operation.ORA, 0x01, "ORA ($%02X,X)");

    private final int opcode;
    private final String mnemonic;
    private final Operation operation;

    LdIndexedIndirect(
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
        int address = AddressingMode.INDEXED_INDIRECT.getEffectiveAddress(registers, peeker);
        registers.a(operation.apply(registers,peeker.peek(address)));
        registers.incrementPc(1);
        return 6;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }

    @Override
    public List<Integer> assemble(final Integer parameter) {
        return ImmutableList.of(opcode(), Word.lo(parameter));
    }
}
