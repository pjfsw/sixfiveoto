package com.pjfsw.sixfiveoto;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.pjfsw.sixfiveoto.instruction.Instruction;
import com.pjfsw.sixfiveoto.instruction.Inx;
import com.pjfsw.sixfiveoto.instruction.Jmp.Absolute;
import com.pjfsw.sixfiveoto.instruction.Lda;
import com.pjfsw.sixfiveoto.instruction.Ldx;
import com.pjfsw.sixfiveoto.instruction.Nop;
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
            .put(Absolute.OPCODE, new Absolute())
            .put(Ldx.Immediate.OPCODE, new Ldx.Immediate())
            .put(Lda.Immediate.OPCODE, new Lda.Immediate())
            .put(Lda.Absolute.OPCODE, new Lda.Absolute())
            .put(Lda.AbsoluteX.OPCODE, new Lda.AbsoluteX())
            .put(Inx.OPCODE, new Inx())
            .put(Nop.OPCODE, new Nop())
            .build();
        reset();
    }

    public void reset() {
        registers.pc = Memory.read16Bit(addressDecoder, RESET_VECTOR);
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
            mnemonic = instruction.getMnemonic(Memory.read16Bit(addressDecoder, registers.pc+1));
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
