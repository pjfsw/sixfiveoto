package com.pjfsw.sixfiveoto.instruction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Jmp {
    public static class Absolute implements Instruction {
        public static final int OPCODE = 0x4C;

        @Override
        public int execute(final Registers registers, Peeker peek, Poker poke) {
            registers.pc = Memory.readWord(peek, registers.pc);
            return 3;
        }

        @Override
        public String getMnemonic(Integer word) {
            return "JMP " + Memory.format(word);
        }
    }
}
