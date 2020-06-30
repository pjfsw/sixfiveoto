package com.pjfsw.sixfiveoto.instruction;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Jsr implements Instruction {
    public static final int OPCODE = 0x20;

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        registers.push(poker, Word.hi(registers.pc+1));
        registers.push(poker, Word.lo(registers.pc+1));
        registers.pc = Memory.readWord(peeker, registers.pc);
        return 6;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("JSR $%04X", parameter);
    }
}
