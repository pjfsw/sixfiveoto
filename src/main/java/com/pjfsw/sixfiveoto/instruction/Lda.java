package com.pjfsw.sixfiveoto.instruction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Lda  {
    public static class Absolute implements Instruction {
        public static final int OPCODE = 0xAD;

        @Override
        public int execute(final Registers registers, Peeker peek, Poker poke) {
            registers.a((peek.peek(Memory.readWord(peek, registers.pc))));
            registers.incrementPc(2);
            return 4;
        }

        @Override
        public String getMnemonic(final Integer word) {
            return String.format("LDA $%04X", word);
        }

        @Override
        public List<Integer> assemble(final Integer word) {
            return ImmutableList.of(OPCODE, Word.lo(word), Word.hi(word));
        }
    }

    public static class AbsoluteX implements Instruction {
        public static final int OPCODE = 0xBD;

        @Override
        public int execute(final Registers registers, Peeker peek, Poker poke) {
            registers.a(peek.peek(Memory.add(Memory.readWord(peek, registers.pc), registers.x())));
            int cycles = 4 + Memory.penalty(registers.pc, Memory.add(registers.pc, registers.x()));
            registers.incrementPc(2);
            return cycles;
        }

        @Override
        public String getMnemonic(final Integer word) {
            return String.format("LDA $%04X,X", word);
        }

        @Override
        public List<Integer> assemble(final Integer word) {
            return ImmutableList.of(OPCODE, Word.lo(word), Word.hi(word));
        }
    }

    public static class Immediate implements Instruction {
        public static final int OPCODE = 0xA9;

        @Override
        public int execute(final Registers registers, Peeker peek, Poker poke) {
            registers.a(peek.peek(registers.pc));
            registers.incrementPc(1);
            return 2;
        }

        @Override
        public String getMnemonic(final Integer word) {
            return String.format("LDA #$%02X", word & 0xFF );
        }

        @Override
        public List<Integer> assemble(final Integer word) {
            return ImmutableList.of(OPCODE, Word.lo(word));
        }
    }
}
