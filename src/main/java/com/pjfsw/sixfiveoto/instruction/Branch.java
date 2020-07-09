package com.pjfsw.sixfiveoto.instruction;

import java.util.function.Function;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum Branch implements Instruction {
    BEQ((registers)-> registers.z , 0xF0, "BEQ"),
    BMI((registers)-> registers.n, 0x30, "BMI"),
    BNE((registers)-> !registers.z, 0xD0, "BNE"),
    BPL((registers)-> !registers.n, 0x10, "BPL"),
    BCS((registers)-> registers.c, 0xB0, "BCS"),
    BCC((registers)-> !registers.c, 0x90, "BCC"),
    BVS((registers)-> registers.v, 0x70, "BVS"),
    BVC((registers)-> !registers.v, 0x50, "BVC"),
    // 65C02 instructions
    BRA((registers)-> true, 0x80, "BRA");
    ;

    private final Function<Registers, Boolean> branchEvaluator;
    private final int opcode;
    private final String mnemonic;

    Branch(Function<Registers,Boolean> branchEvaluator, int opcode, String mnemonic) {
        this.branchEvaluator = branchEvaluator;
        this.opcode = opcode;
        this.mnemonic = mnemonic;
    }

    public int opcode() {
        return opcode;
    }

    @Override
    public int execute(final Registers registers, final Peeker peeker, final Poker poker) {
        if (branchEvaluator.apply(registers)) {
            int offset = peeker.peek(registers.pc);
            registers.incrementPc(1);
            int effectiveAddress = Memory.addSigned(registers.pc, offset);
            int penalty = Memory.penalty(registers.pc, effectiveAddress);
            registers.pc = effectiveAddress;
            return 3 + penalty;
        } else {
            registers.incrementPc(1);
            return 2;
        }
    }

    @Override
    public String getMnemonic() {
        return mnemonic;
    }

    @Override
    public MnemonicFormatter getMnemonicFormatter() {
        return MnemonicFormatter.RELATIVE;
    }
}
