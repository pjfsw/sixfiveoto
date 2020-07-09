package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Consumer;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum ModifyFlags implements Instruction {
    CLC((registers)->registers.c = false, 0x18, "CLC"),
    SEC((registers)->registers.c = true, 0x38, "SEC"),
    CLV((registers)->registers.v = false, 0xB8, "CLV");

    private final Consumer<Registers> modifier;
    private final int opcode;
    private final String mnemonic;

    ModifyFlags(Consumer<Registers> modifier, int opcode, String mnemonic) {
        this.modifier = modifier;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        modifier.accept(registers);
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
