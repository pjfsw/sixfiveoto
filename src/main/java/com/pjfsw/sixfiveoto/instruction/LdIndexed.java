package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdIndexed implements Instruction {
    LDAX(Registers::a, Registers::x, 0xBD, "LDA $%04X,X"),
    LDAY(Registers::a, Registers::y, 0xB9, "LDA $%04X,Y"),
    LDXY(Registers::x, Registers::y, 0xBE, "LDX $%04X,Y"),
    LDYX(Registers::y, Registers::x, 0xBC, "LDY $%04X,X");

    private final int opcode;
    private final String mnemonic;
    private final BiConsumer<Registers, Integer> to;
    private final Function<Registers, Integer> index;

    LdIndexed(BiConsumer<Registers,Integer> to, Function<Registers,Integer> index, int opcode, String mnemonic) {
        this.to = to;
        this.index = index;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        to.accept(registers, peeker.peek(Memory.add(Memory.readWord(peeker, registers.pc), index.apply(registers))));
        int cycles = 4 + Memory.penalty(registers.pc, Memory.add(registers.pc, registers.x()));
        registers.incrementPc(2);
        return cycles;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format(mnemonic, parameter);
    }

    @Override
    public List<Integer> assemble(final Integer parameter) {
        return ImmutableList.of(opcode(), Word.lo(parameter), Word.hi(parameter));
    }
}
