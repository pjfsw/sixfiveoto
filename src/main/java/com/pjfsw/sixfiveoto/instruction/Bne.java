package com.pjfsw.sixfiveoto.instruction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Bne implements Instruction {
    public static final int OPCODE = 0xD0;

    @Override
    public int execute(final Registers registers, final Peeker peek, final Poker poke) {
        return BranchHelper.branch(!registers.z, registers, peek);
    }

    @Override
    public String getMnemonic(final Integer word) {
        return String.format("BNE %02X", word);
    }

    @Override
    public List<Integer> assemble(final Integer word) {
        return ImmutableList.of(OPCODE, word);
    }
}
