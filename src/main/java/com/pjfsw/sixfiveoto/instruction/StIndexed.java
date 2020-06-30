package com.pjfsw.sixfiveoto.instruction;

import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum StIndexed implements Instruction {
    STAX(AddressingMode.INDEXED_X, 0x9D, "X"),
    STAY(AddressingMode.INDEXED_Y, 0x99, "Y")
    ;

    private final int opcode;
    private final String mnemonic;
    private final AddressingMode addressingMode;

    StIndexed(AddressingMode addressingMode, int opcode, String mnemonic) {
        this.addressingMode = addressingMode;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        poker.poke(addressingMode.getEffectiveAddress(registers, peeker), registers.a());
        registers.incrementPc(2);
        return 5;
    }

    @Override
    public String getMnemonic(final Integer parameter) {
        return String.format("STA $%04X,%s", parameter, mnemonic);
    }

}
