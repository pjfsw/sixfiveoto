package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Rts implements Instruction {
    public static final int OPCODE = 0x60;

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        registers.pc = registers.pull(peeker);
        registers.pc |= registers.pull(peeker) << 8;
        registers.pc++;
        return 6;
    }

    @Override
    public String getMnemonic() {
        return "RTS";
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
