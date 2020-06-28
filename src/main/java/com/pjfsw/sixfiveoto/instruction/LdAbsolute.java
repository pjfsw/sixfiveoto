package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdAbsolute implements Instruction {
    AND(Registers::a, Operation.AND, 0x2D, "AND"),
    EOR(Registers::a, Operation.EOR, 0x4D, "EOR"),
    LDA(Registers::a, Operation.EQU, 0xAD, "LDA"),
    LDX(Registers::x, Operation.EQU, 0xAE, "LDX"),
    LDY(Registers::y, Operation.EQU, 0xAC, "LDY"),
    ORA(Registers::a, Operation.ORA, 0x0D, "ORA");

    private final BiConsumer<Registers, Integer> to;
    private final int opcode;
    private final String mnemonic;
    private final Operation operation;

    LdAbsolute(BiConsumer<Registers,Integer> to, Operation operation, int opcode, String mnemonic) {
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
        to.accept(registers, operation.apply(registers, peeker.peek(Memory.readWord(peeker, registers.pc))));
        registers.incrementPc(2);
        return 4;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("%s $%04X", mnemonic, parameter);
    }

    @Override
    public List<Integer> assemble(final Integer parameter) {
        return ImmutableList.of(opcode, Word.lo(parameter), Word.hi(parameter));
    }
}
