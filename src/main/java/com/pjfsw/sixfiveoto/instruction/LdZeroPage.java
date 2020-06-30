package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdZeroPage implements Instruction {
    AND(Registers::a, Operation.AND, 0x25, "AND"),
    EOR(Registers::a, Operation.EOR, 0x45, "EOR"),
    LDA(Registers::a, Operation.EQU, 0xA5, "LDA"),
    LDX(Registers::x, Operation.EQU, 0xA6, "LDX"),
    LDY(Registers::y, Operation.EQU, 0xA4, "LDY"),
    ORA(Registers::a, Operation.ORA, 0x05, "ORA");

    private final BiConsumer<Registers, Integer> to;
    private final int opcode;
    private final String mnemonic;
    private final Operation operation;

    LdZeroPage(BiConsumer<Registers,Integer> to, Operation operation, int opcode, String mnemonic) {
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
        int zpAddress = peeker.peek(registers.pc);
        to.accept(registers, operation.apply(registers, peeker.peek(zpAddress)));
        registers.incrementPc(1);
        return 3;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("%s $%02X", mnemonic, parameter);
    }
}
