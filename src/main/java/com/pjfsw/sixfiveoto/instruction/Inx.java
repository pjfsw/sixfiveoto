package com.pjfsw.sixfiveoto.instruction;

import java.util.Collection;
import java.util.Collections;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Inx implements Instruction {
    public static final int OPCODE = 0xE8;
    @Override
    public int execute(final Registers registers, Peeker peek, Poker poke) {
        registers.x = registers.add(registers.x, 1);
        return 2;
    }

    @Override
    public String getMnemonic(final Integer word) {
        return "INX";
    }

    @Override
    public Collection<Integer> assemble(final Integer word) {
        return Collections.singletonList(OPCODE);
    }
}
