package com.pjfsw.sixfiveoto.instruction;

import static java.util.Collections.singletonList;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum Transfer implements Instruction {
    TAX(Registers::a, Registers::x, 0xaa, "TAX"),
    TAY(Registers::a, Registers::y, 0xa8, "TAY"),
    TSX(Registers::sp, Registers::x, 0xba, "TSX"),
    TXA(Registers::x, Registers::a, 0x8a, "TXA"),
    TXS(Registers::x, Registers::sp, 0x9a, "TXS"),
    TYA(Registers::y, Registers::a, 0x98, "TYA"),

    ;

    private final Function<Registers,Integer> from;
    private final BiConsumer<Registers,Integer> to;
    private final int opcode;
    private final String mnemonic;

    Transfer(Function<Registers,Integer> from, BiConsumer<Registers,Integer> to, int opcode, String mnemonic) {
        this.from = from;
        this.to = to;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return this.opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        to.accept(registers, from.apply(registers));
        return 2;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return mnemonic;
    }
}
