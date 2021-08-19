package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Cli implements Instruction {
    public static final int OPCODE = 0x58;

    @Override
    public int execute(final Registers registers, Peeker peek, Poker poke) {
        registers.i(false);
        return 2;
    }

    @Override
    public String getMnemonic() {
        return "CLI";
    }

    @Override
    public MnemonicFormatter getMnemonicFormatter() {
        return MnemonicFormatter.IMPLIED;
    }

    @Override
    public int length() {
        return 1;
    }

}
