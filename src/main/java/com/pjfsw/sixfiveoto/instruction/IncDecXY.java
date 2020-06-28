package com.pjfsw.sixfiveoto.instruction;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum IncDecXY implements Instruction {
    INX(Registers::x, Registers::x, 1, 0xE8, "INX"),
    INY(Registers::y, Registers::y, 1, 0xC8, "INY"),
    DEX(Registers::x, Registers::x, -1, 0xCA, "DEX"),
    DEY(Registers::y, Registers::y, -1, 0x88, "DEY");

    private final Function<Registers, Integer> from;
    private final BiConsumer<Registers, Integer> to;
    private final int direction;
    private final int opcode;
    private final String mnemonic;

    IncDecXY(Function<Registers,Integer> from, BiConsumer<Registers,Integer> to, int direction, int opcode, String mnemonic) {
        this.from = from;
        this.to = to;
        this.direction = direction;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, Peeker peek, Poker poke) {
        to.accept(registers, registers.add(from.apply(registers), direction));
        return 2;
    }

    @Override
    public String getMnemonic(final Integer word) {
        return mnemonic;
    }

    @Override
    public List<Integer> assemble(final Integer word) {
        return Collections.singletonList(opcode);
    }
}
