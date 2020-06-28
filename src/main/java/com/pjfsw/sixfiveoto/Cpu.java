package com.pjfsw.sixfiveoto;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.pjfsw.sixfiveoto.instruction.Branch;
import com.pjfsw.sixfiveoto.instruction.Dex;
import com.pjfsw.sixfiveoto.instruction.Instruction;
import com.pjfsw.sixfiveoto.instruction.Inx;
import com.pjfsw.sixfiveoto.instruction.Jmp;
import com.pjfsw.sixfiveoto.instruction.Jsr;
import com.pjfsw.sixfiveoto.instruction.Lda;
import com.pjfsw.sixfiveoto.instruction.Ldx;
import com.pjfsw.sixfiveoto.instruction.Nop;
import com.pjfsw.sixfiveoto.instruction.Rts;
import com.pjfsw.sixfiveoto.instruction.Sta;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Cpu {
    private static final int RESET_VECTOR = 0xFFFC;
    private static final int IRQ_VECTOR = 0xFFFE;
    private static final int NMI_VECTOR = 0xFFFA;

    private final Map<Integer, Instruction> instructions;
    private final AddressDecoder addressDecoder;
    private final Registers registers;
    private long totalCycles = 0;

    public Cpu(AddressDecoder addressDecoder, Registers registers) {
        this.addressDecoder = addressDecoder;
        this.registers = registers;
        instructions = ImmutableMap.<Integer, Instruction>builder()
            .put(Branch.BEQ.opcode(), Branch.BEQ)
            .put(Branch.BMI.opcode(), Branch.BMI)
            .put(Branch.BNE.opcode(), Branch.BNE)
            .put(Branch.BPL.opcode(), Branch.BPL)
            .put(Dex.OPCODE, new Dex())
            .put(Jmp.Absolute.OPCODE, new Jmp.Absolute())
            .put(Jsr.OPCODE, new Jsr())
            .put(Lda.Immediate.OPCODE, new Lda.Immediate())
            .put(Lda.Absolute.OPCODE, new Lda.Absolute())
            .put(Lda.AbsoluteX.OPCODE, new Lda.AbsoluteX())
            .put(Ldx.Immediate.OPCODE, new Ldx.Immediate())
            .put(Inx.OPCODE, new Inx())
            .put(Nop.OPCODE, new Nop())
            .put(Rts.OPCODE, new Rts())
            .put(Sta.Absolute.OPCODE, new Sta.Absolute())
            .put(Sta.AbsoluteX.OPCODE, new Sta.AbsoluteX())
            .build();
        reset();
    }

    public void reset() {
        registers.pc = Memory.readWord(addressDecoder, RESET_VECTOR);
        totalCycles = 0;
    }

    public long getCycles() {
        return totalCycles;
    }

    public String toString()
    {
        Instruction instruction = instructions.get(addressDecoder.peek(registers.pc));
        String mnemonic;
        if (instruction != null) {
            mnemonic = instruction.getMnemonic(Memory.readWord(addressDecoder, registers.pc+1));
        } else {
            mnemonic = "???";
        }
        return String.format("%-12s %s", mnemonic, registers.toString());

    }

    public int next() {
        Instruction instruction = instructions.get(addressDecoder.peek(registers.pc));
        registers.incrementPc(1);
        if (instruction != null) {
            int cycles = instruction.execute(registers, addressDecoder, addressDecoder);;
            totalCycles += cycles;
            return cycles;
        } else {
            return 0;
        }
    }

}
