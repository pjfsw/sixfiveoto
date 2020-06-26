package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Consumer;
import java.util.function.Function;

import com.pjfsw.sixfiveoto.registers.Registers;

public class Nop implements Instruction {
    @Override
    public int execute(final Registers registers, final Function<Integer, Integer> peek,
        final Consumer<Integer> poke) {
        return 2;
    }

    @Override
    public String getMnemonic(Integer sixteenBitValue) {
        return "NOP";
    }
}
