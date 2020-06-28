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
    LDA(Registers::a, 0xAD, "LDA"),
    LDX(Registers::x, 0xAE, "LDX"),
    LDY(Registers::y, 0xAC, "LDY");

    private final BiConsumer<Registers, Integer> to;
    private final int opcode;
    private final String mnemonic;

    LdAbsolute(BiConsumer<Registers,Integer> to, int opcode, String mnemonic) {
        this.to = to;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        to.accept(registers, peeker.peek(Memory.readWord(peeker, registers.pc)));
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
