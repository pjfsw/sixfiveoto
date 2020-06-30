package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum StAbsolute implements Instruction {
    STA(Registers::a, 0x8D, "STA"),
    STX(Registers::x, 0x8E, "STX"),
    STY(Registers::y, 0x8C, "STY");

    private final int opcode;
    private final String mnemonic;
    private final Function<Registers, Integer> from;

    StAbsolute(Function<Registers,Integer> from, int opcode, String mnemonic) {
        this.from = from;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        poker.poke(Memory.readWord(peeker, registers.pc), from.apply(registers));
        registers.incrementPc(2);
        return 4;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("%s $%04X", mnemonic, parameter);
    }
}
