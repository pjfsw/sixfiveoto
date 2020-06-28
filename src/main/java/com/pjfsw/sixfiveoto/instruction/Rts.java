package com.pjfsw.sixfiveoto.instruction;

import static java.util.Collections.singletonList;

import java.util.List;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Rts implements Instruction {
    public static final int OPCODE = 0x60;

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        registers.pc = registers.sp(peeker);
        registers.pc |= registers.sp(peeker) << 8;
        registers.pc++;
        return 6;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return "RTS";
    }

    @Override
    public List<Integer> assemble(final Integer parameter) {
        return singletonList(OPCODE);
    }
}
