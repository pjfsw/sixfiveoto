package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class JmpIndexedIndirect implements Instruction {
    public static final int OPCODE = 0x7C;

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int pointer = Memory.add(Memory.readWord(peeker, registers.pc), registers.x());
        registers.pc = Memory.readWord(peeker, pointer);
        return 6;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("JMP $%04X", parameter);
    }
}
