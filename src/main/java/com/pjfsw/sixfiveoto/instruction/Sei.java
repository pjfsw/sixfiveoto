package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Sei implements Instruction {
    public static final int OPCODE = 0x78;

    @Override
    public int execute(final Registers registers, Peeker peek, Poker poke) {
        return 2;
    }

    @Override
    public String getMnemonic() {
        return "SEI";
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
