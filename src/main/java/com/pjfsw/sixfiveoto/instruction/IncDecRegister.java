package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiConsumer;
import java.util.function.Function;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum IncDecRegister implements Instruction {
    DEX(Registers::x, Registers::x, -1, 0xCA, "DEX"),
    INX(Registers::x, Registers::x, 1, 0xE8, "INX"),
    DEY(Registers::y, Registers::y, -1, 0x88, "DEY"),
    INY(Registers::y, Registers::y, 1, 0xC8, "INY"),
    // 65C02 instructions
    DECA(Registers::a, Registers::a, -1, 0x3a, "DEC"),
    INCA(Registers::a, Registers::a, 1, 0x1a, "INC");


    private final Function<Registers, Integer> from;
    private final BiConsumer<Registers, Integer> to;
    private final int direction;
    private final int opcode;
    private final String mnemonic;

    IncDecRegister(Function<Registers,Integer> from, BiConsumer<Registers,Integer> to, int direction, int opcode, String mnemonic) {
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
    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public MnemonicFormatter getMnemonicFormatter() {
        return MnemonicFormatter.IMPLIED;
    }
}
