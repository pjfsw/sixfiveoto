package com.pjfsw.sixfiveoto.instruction;

import java.util.Collections;
import java.util.List;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Nop implements Instruction {
    public static final int OPCODE = 0xEA;

    @Override
    public int execute(final Registers registers, Peeker peek, Poker poke) {
        return 2;
    }

    @Override
    public String getMnemonic(Integer word) {
        return "NOP";
    }
}
