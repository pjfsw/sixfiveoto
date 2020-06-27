package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.registers.Registers;

public class BranchHelper {
    public static int branch(boolean branch, Registers registers, Peeker peek) {
        if (branch) {
            int offset = peek.peek(registers.pc);
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
}
