package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public interface Instruction {
    /**
     * Execute the instruction
     *
     * @return
     */
    int execute(final Registers registers, Peeker peeker, Poker poker);

    String getMnemonic();

    MnemonicFormatter getMnemonicFormatter();

    int length();

}
