package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiConsumer;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum StackPull implements Instruction {
    PLA(Registers::a, 0x68, "PLA"),
    PLP(Registers::sr, 0x28, "PLP");

    private final BiConsumer<Registers, Integer> destination;
    private final int opcode;
    private final String mnemonic;

    StackPull(BiConsumer<Registers, Integer> destination, int opcode, String mnemonic) {
        this.destination = destination;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        destination.accept(registers, registers.pull(peeker));
        return 4;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return mnemonic;
    }
}
