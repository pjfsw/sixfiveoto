package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum StIndexed implements Instruction {
    STAX(Registers::x, 0x9D, "X"),
    STAY(Registers::y, 0x99, "Y")
    ;

    private final Function<Registers, Integer> index;
    private final int opcode;
    private final String mnemonic;

    StIndexed(Function<Registers,Integer> index, int opcode, String mnemonic) {
        this.index = index;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        poker.poke(Memory.add(Memory.readWord(peeker, registers.pc), index.apply(registers)), registers.a());
        registers.incrementPc(2);
        return 5;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("STA $%04X,%s", parameter, mnemonic);
    }

    @Override
    public List<Integer> assemble(final Integer parameter) {
        return ImmutableList.of(opcode(), Word.lo(parameter), Word.hi(parameter));
    }
}
