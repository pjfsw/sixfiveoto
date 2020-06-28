package com.pjfsw.sixfiveoto;

import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.pjfsw.sixfiveoto.instruction.Branch;
import com.pjfsw.sixfiveoto.instruction.Dec;
import com.pjfsw.sixfiveoto.instruction.Inc;
import com.pjfsw.sixfiveoto.instruction.Instruction;
import com.pjfsw.sixfiveoto.instruction.IncDecXY;
import com.pjfsw.sixfiveoto.instruction.Jmp;
import com.pjfsw.sixfiveoto.instruction.Jsr;
import com.pjfsw.sixfiveoto.instruction.LdAbsolute;
import com.pjfsw.sixfiveoto.instruction.LdImmediate;
import com.pjfsw.sixfiveoto.instruction.LdIndexed;
import com.pjfsw.sixfiveoto.instruction.Nop;
import com.pjfsw.sixfiveoto.instruction.Pha;
import com.pjfsw.sixfiveoto.instruction.Pla;
import com.pjfsw.sixfiveoto.instruction.Rts;
import com.pjfsw.sixfiveoto.instruction.StAbsolute;
import com.pjfsw.sixfiveoto.instruction.StIndexed;
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
            .putAll(Arrays.stream(Branch.values())
                .collect(toMap(
                    Branch::opcode,
                    Function.identity())))
            .put(Dec.DecAbsolute.OPCODE, new Dec.DecAbsolute())
            .put(Dec.DecIndexed.OPCODE, new Dec.DecIndexed())
            .put(Jmp.Absolute.OPCODE, new Jmp.Absolute())
            .put(Jsr.OPCODE, new Jsr())
            .putAll(Arrays.stream(LdImmediate.values())
                .collect(toMap(
                    LdImmediate::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(LdAbsolute.values())
                .collect(toMap(
                    LdAbsolute::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(LdIndexed.values())
                .collect(toMap(
                    LdIndexed::opcode,
                    Function.identity())))
            .put(Inc.IncAbsolute.OPCODE, new Inc.IncAbsolute())
            .put(Inc.IncIndexed.OPCODE, new Inc.IncIndexed())
            .putAll(Arrays.stream(IncDecXY.values())
                .collect(toMap(
                    IncDecXY::opcode,
                    Function.identity())))
            .put(Nop.OPCODE, new Nop())
            .put(Pha.OPCODE, new Pha())
            .put(Pla.OPCODE, new Pla())
            .put(Rts.OPCODE, new Rts())
            .putAll(Arrays.stream(StAbsolute.values())
                .collect(toMap(
                    StAbsolute::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(StIndexed.values())
                .collect(toMap(
                    StIndexed::opcode,
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
