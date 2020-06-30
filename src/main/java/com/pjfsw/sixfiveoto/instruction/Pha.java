package com.pjfsw.sixfiveoto.instruction;

import static java.util.Collections.singletonList;

import java.util.List;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Pha implements Instruction {
    public static final int OPCODE = 0x48;

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        registers.push(poker, registers.a());
        return 3;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return "PHA";
    }
}
