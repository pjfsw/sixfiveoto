package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Function;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum StackPush implements Instruction {
    PHA(Registers::a, 0x48, "PHA"),
    PHP(Registers::sr, 0x08, "PHP"),
    // 65C02 specific
    PHX(Registers::x, 0xDA, "PHX"),
    PHY(Registers::y, 0x5A, "PHY");

    private final Function<Registers, Integer> source;
    private final int opcode;
    private final String mnemonic;

    StackPush(Function<Registers, Integer> source, int opcode, String mnemonic) {
        this.source = source;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        registers.push(poker, source.apply(registers));
        return 3;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return mnemonic;
    }
}
