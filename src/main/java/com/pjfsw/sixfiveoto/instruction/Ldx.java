package com.pjfsw.sixfiveoto.instruction;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Ldx {
    public static class Immediate implements Instruction {
        public static final int OPCODE = 0xA2;

        @Override
        public int execute(final Registers registers, Peeker peek, Poker poke) {
            registers.x = peek.peek(registers.pc);
            registers.incrementPc(1);
            return 2;
        }

        @Override
        public String getMnemonic(final Integer word) {
            return String.format("LDX #$%02X", word & 0xFF );
        }

        @Override
        public Collection<Integer> assemble(final Integer word) {
            return ImmutableList.of(OPCODE, Word.lo(word));
        }
    }
}
