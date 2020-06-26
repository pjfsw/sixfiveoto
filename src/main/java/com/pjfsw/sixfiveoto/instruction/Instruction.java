package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Consumer;
import java.util.function.Function;

import com.pjfsw.sixfiveoto.registers.Registers;

public interface Instruction {
    /**
     * Execute the instruction
     *
     * @return
     */
    int execute(final Registers registers, Function<Integer, Integer> peek, Consumer<Integer> poke);

    String getMnemonic(Integer sixteenBitValue);
}
