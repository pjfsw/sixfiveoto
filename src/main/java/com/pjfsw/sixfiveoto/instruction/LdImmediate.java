package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdImmediate implements Instruction {
    LDA(Registers::a, 0xA9, "LDA"),
    LDX(Registers::x, 0xA2, "LDX"),
    LDY(Registers::y, 0xA0, "LDY");

    private final BiConsumer<Registers, Integer> to;
    private final int opcode;
    private final String mnemonic;

    LdImmediate(BiConsumer<Registers,Integer> to, int opcode, String mnemonic) {
        this.to = to;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        to.accept(registers, peeker.peek(registers.pc));
        registers.incrementPc(1);
        return 2;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("%s #$%02X", mnemonic, parameter & 0xFF );
    }

    @Override
    public List<Integer> assemble(final Integer parameter) {
        return ImmutableList.of(opcode, Word.lo(parameter));
    }
}
