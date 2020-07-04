package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Jmp implements Instruction {
    public static final int OPCODE = 0x4C;

    @Override
    public int execute(final Registers registers, Peeker peek, Poker poke) {
        registers.pc = Memory.readWord(peek, registers.pc);
        return 3;
    }

    @Override
    public String getMnemonic(Integer word) {
        return String.format("JMP ",Memory.format(word));
    }
}
