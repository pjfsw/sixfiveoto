package com.pjfsw.sixfiveoto.instruction;

import static java.util.Collections.singletonList;

import java.util.List;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Pla implements Instruction {
    public static final int OPCODE = 0x68;

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        registers.a(registers.pull(peeker));
        return 4;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return "PLA";
    }
}
