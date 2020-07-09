package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiConsumer;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum LdImmediate implements Instruction {
    AND(Registers::a, LoadOperation.AND, 0x29, "AND"),
    EOR(Registers::a, LoadOperation.EOR, 0x49, "EOR"),
    LDA(Registers::a, LoadOperation.LD, 0xA9, "LDA"),
    LDX(Registers::x, LoadOperation.LD, 0xA2, "LDX"),
    LDY(Registers::y, LoadOperation.LD, 0xA0, "LDY"),
    ORA(Registers::a, LoadOperation.ORA, 0x09, "ORA");

    private final BiConsumer<Registers, Integer> to;
    private final int opcode;
    private final String mnemonic;
    private final LoadOperation loadOperation;

    LdImmediate(BiConsumer<Registers,Integer> to, LoadOperation loadOperation, int opcode, String mnemonic) {
        this.to = to;
        this.loadOperation = loadOperation;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        to.accept(registers, loadOperation.apply(registers, peeker.peek(registers.pc)));
        registers.incrementPc(1);
        return 2;
    }

    @Override
    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public MnemonicFormatter getMnemonicFormatter() {
        return MnemonicFormatter.IMMEDIATE;
    }
}
