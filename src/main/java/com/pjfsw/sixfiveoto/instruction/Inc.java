package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Inc {
    public static class IncAbsolute implements Instruction {
        public static final int OPCODE = 0xEE;

        @Override
        public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
            int address = Memory.readWord(peeker, registers.pc);
            poker.poke(address, registers.add(peeker.peek(address), 1));
            return 6;
        }

        @Override
        public String getMnemonic(final Integer parameter) {
            return String.format("INC $%04X", parameter);
        }

        @Override
        public List<Integer> assemble(final Integer parameter) {
            return ImmutableList.of(OPCODE, Word.lo(parameter), Word.hi(parameter));
        }
    }
}
