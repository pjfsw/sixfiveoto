package com.pjfsw.sixfiveoto;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.pjfsw.sixfiveoto.instruction.Adc;
import com.pjfsw.sixfiveoto.instruction.Bit;
import com.pjfsw.sixfiveoto.instruction.Branch;
import com.pjfsw.sixfiveoto.instruction.Cmp;
import com.pjfsw.sixfiveoto.instruction.IncDec;
import com.pjfsw.sixfiveoto.instruction.Instruction;
import com.pjfsw.sixfiveoto.instruction.IncDecXY;
import com.pjfsw.sixfiveoto.instruction.Jmp;
import com.pjfsw.sixfiveoto.instruction.JmpIndirect;
import com.pjfsw.sixfiveoto.instruction.Jsr;
import com.pjfsw.sixfiveoto.instruction.LdImmediate;
import com.pjfsw.sixfiveoto.instruction.LdMemory;
import com.pjfsw.sixfiveoto.instruction.ModifyFlags;
import com.pjfsw.sixfiveoto.instruction.Nop;
import com.pjfsw.sixfiveoto.instruction.Pha;
import com.pjfsw.sixfiveoto.instruction.Pla;
import com.pjfsw.sixfiveoto.instruction.Rts;
import com.pjfsw.sixfiveoto.instruction.StMemory;
import com.pjfsw.sixfiveoto.instruction.Transfer;
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
            .putAll(Arrays.stream(Adc.values())
                .collect(toMap(
                    Adc::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(Bit.values())
                .collect(toMap(
                    Bit::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(Branch.values())
                .collect(toMap(
                    Branch::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(Cmp.values())
                .collect(toMap(
                    Cmp::opcode,
                    Function.identity())))
            .put(Jmp.OPCODE, new Jmp())
            .put(JmpIndirect.OPCODE, new JmpIndirect())
            .put(Jsr.OPCODE, new Jsr())
            .putAll(Arrays.stream(LdImmediate.values())
                .collect(toMap(
                    LdImmediate::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(LdMemory.values())
                .collect(toMap(
                    LdMemory::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(IncDec.values())
                .collect(toMap(
                    IncDec::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(IncDecXY.values())
                .collect(toMap(
                    IncDecXY::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(ModifyFlags.values())
                .collect(toMap(
                    ModifyFlags::opcode,
                    Function.identity())))
            .put(Nop.OPCODE, new Nop())
            .put(Pha.OPCODE, new Pha())
            .put(Pla.OPCODE, new Pla())
            .put(Rts.OPCODE, new Rts())
            .putAll(Arrays.stream(StMemory.values())
                .collect(toMap(
                    StMemory::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(Transfer.values())
                .collect(toMap(
                    Transfer::opcode,
                    Function.identity())))
            .build();
        System.out.println(String.format("%d opcodes supported", instructions.size()));
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
        int opcode = addressDecoder.peek(registers.pc);
        Instruction instruction = instructions.get(opcode);
        String mnemonic;
        if (instruction != null) {
            mnemonic = instruction.getMnemonic(Memory.readWord(addressDecoder, registers.pc+1));
        } else {
            mnemonic = String.format("???(%02X)", opcode);
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
