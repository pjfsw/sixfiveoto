package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdImmediate implements Instruction {
    AND(Registers::a, Operation.AND, 0x29, "AND"),
    EOR(Registers::a, Operation.EOR, 0x49, "EOR"),
    LDA(Registers::a, Operation.EQU, 0xA9, "LDA"),
    LDX(Registers::x, Operation.EQU, 0xA2, "LDX"),
    LDY(Registers::y, Operation.EQU, 0xA0, "LDY"),
    ORA(Registers::a, Operation.ORA, 0x09, "ORA");

    private final BiConsumer<Registers, Integer> to;
    private final int opcode;
    private final String mnemonic;
    private final Operation operation;

    LdImmediate(BiConsumer<Registers,Integer> to, Operation operation, int opcode, String mnemonic) {
        this.to = to;
        this.operation = operation;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        to.accept(registers, operation.apply(registers, peeker.peek(registers.pc)));
        registers.incrementPc(1);
        return 2;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("%s #$%02X", mnemonic, parameter & 0xFF );
    }
}
