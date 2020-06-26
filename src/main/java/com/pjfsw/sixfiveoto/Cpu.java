package com.pjfsw.sixfiveoto;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.pjfsw.sixfiveoto.instruction.Instruction;
import com.pjfsw.sixfiveoto.instruction.Jmp;
import com.pjfsw.sixfiveoto.instruction.Nop;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Cpu {
    private static final int RESET_VECTOR = 0xFFFC;
    private static final int IRQ_VECTOR = 0xFFFE;
    private static final int NMI_VECTOR = 0xFFFA;

    private final Map<Integer, Instruction> instructions;
    private final AddressDecoder addressDecoder;
    private final Registers registers;

    public Cpu(AddressDecoder addressDecoder, Registers registers) {
        this.addressDecoder = addressDecoder;
        this.registers = registers;
        instructions = ImmutableMap.<Integer, Instruction>builder()
            .put(0x4C, new Jmp())
            .put(0xEA, new Nop())
            .build();
    }

    public void reset() {
        registers.pc = Memory.read16Bit(addressDecoder, RESET_VECTOR);
    }

    public String toString()
    {
        Instruction instruction = instructions.get(addressDecoder.apply(registers.pc));
        if (instruction != null) {
            return instruction.getMnemonic(Memory.read16Bit(addressDecoder, registers.pc+1));
        } else {
            return "???";
        }
    }

    public int next() {
        Instruction instruction = instructions.get(addressDecoder.apply(registers.pc));
        registers.incrementPc(1);
        if (instruction != null) {
            return instruction.execute(registers, addressDecoder, addressDecoder);
        } else {
            return 0;
        }
    }

}
