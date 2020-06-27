package com.pjfsw.sixfiveoto.instruction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Sta {
    public static class Absolute implements Instruction {
        public static final int OPCODE = 0x8D;

        @Override
        public int execute(final Registers registers, final Peeker peek, final Poker poke) {
            poke.poke(Memory.readWord(peek, registers.pc), registers.a());
            registers.incrementPc(2);
            return 4;
        }

        @Override
        public String getMnemonic(final Integer word) {
            return String.format("STA $%04X", word);
        }

        @Override
        public List<Integer> assemble(final Integer word) {
            return ImmutableList.of(OPCODE, Word.lo(word), Word.hi(word));
        }
    }

    public static class AbsoluteX implements Instruction {
        public static final int OPCODE = 0x9D;

        @Override
        public int execute(final Registers registers, final Peeker peek, final Poker poke) {
            poke.poke(Memory.add(Memory.readWord(peek, registers.pc), registers.x()), registers.a());
            return 5;
        }

        @Override
        public String getMnemonic(final Integer word) {
            return String.format("STA $%04X,X", word);
        }

        @Override
        public List<Integer> assemble(final Integer word) {
            return ImmutableList.of(OPCODE, Word.lo(word), Word.hi(word));
        }
    }
}
