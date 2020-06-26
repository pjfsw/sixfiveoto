package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Consumer;
import java.util.function.Function;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Jmp implements Instruction {
    @Override
    public int execute(final Registers registers, final Function<Integer, Integer> peek,
        final Consumer<Integer> poke) {
        registers.pc = Memory.read16Bit(peek, registers.pc);
        return 3;
    }

    @Override
    public String getMnemonic(Integer sixteenBitValue) {
        return "JMP " + Memory.format(sixteenBitValue);
    }
}
