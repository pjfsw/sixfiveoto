package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public class JmpIndirect implements Instruction {
    public static final int OPCODE = 0x6C;

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        int pointer = Memory.readWord(peeker, registers.pc);
        registers.pc = Memory.readWord(peeker, pointer);
        return 6;
    }

    @Override
    public String getMnemonic() {
        return "JMP";
    }

    @Override
    public MnemonicFormatter getMnemonicFormatter() {
        return MnemonicFormatter.INDIRECT_ADDRESS;
    }

    @Override
    public int length() {
        return 3;
    }

}
