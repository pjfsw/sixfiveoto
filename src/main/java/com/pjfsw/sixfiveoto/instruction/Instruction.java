package com.pjfsw.sixfiveoto.instruction;

import java.util.List;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public interface Instruction {
    /**
     * Execute the instruction
     *
     * @return
     */
    int execute(final Registers registers, Peeker peek, Poker poke);

    String getMnemonic(Integer word);

    List<Integer> assemble(Integer word);
}
