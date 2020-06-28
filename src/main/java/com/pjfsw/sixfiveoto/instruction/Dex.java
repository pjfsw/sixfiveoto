package com.pjfsw.sixfiveoto.instruction;

import java.util.Collections;
import java.util.List;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Dex implements Instruction {
    public static final int OPCODE = 0xCA;
    @Override
    public int execute(final Registers registers, Peeker peek, Poker poke) {
        registers.x(registers.add(registers.x(), -1));
        return 2;
    }

    @Override
    public String getMnemonic(final Integer word) {
        return "DEX";
    }

    @Override
    public List<Integer> assemble(final Integer word) {
        return Collections.singletonList(OPCODE);
    }
}
