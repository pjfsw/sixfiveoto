package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Rti implements Instruction {
    public static final int OPCODE = 0x40;

    @Override
    public int execute(final Registers registers, Peeker peek, Poker poke) {
        registers.sr(registers.pull(peek));
        registers.pc = registers.pull(peek);
        registers.pc |= registers.pull(peek) << 8;
        return 6;
    }

    @Override
    public String getMnemonic() {
        return "RTI";
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
