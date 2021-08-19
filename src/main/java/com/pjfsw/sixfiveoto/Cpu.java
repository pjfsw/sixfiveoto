package com.pjfsw.sixfiveoto;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import com.pjfsw.sixfiveoto.instruction.Adc;
import com.pjfsw.sixfiveoto.instruction.Bit;
import com.pjfsw.sixfiveoto.instruction.Branch;
import com.pjfsw.sixfiveoto.instruction.Cli;
import com.pjfsw.sixfiveoto.instruction.Cmp;
import com.pjfsw.sixfiveoto.instruction.IncDec;
import com.pjfsw.sixfiveoto.instruction.Instruction;
import com.pjfsw.sixfiveoto.instruction.IncDecRegister;
import com.pjfsw.sixfiveoto.instruction.Jmp;
import com.pjfsw.sixfiveoto.instruction.JmpIndexedIndirect;
import com.pjfsw.sixfiveoto.instruction.JmpIndirect;
import com.pjfsw.sixfiveoto.instruction.Jsr;
import com.pjfsw.sixfiveoto.instruction.LdImmediate;
import com.pjfsw.sixfiveoto.instruction.LdMemory;
import com.pjfsw.sixfiveoto.instruction.ModifyFlags;
import com.pjfsw.sixfiveoto.instruction.Nop;
import com.pjfsw.sixfiveoto.instruction.RotateShift;
import com.pjfsw.sixfiveoto.instruction.Rti;
import com.pjfsw.sixfiveoto.instruction.Sei;
import com.pjfsw.sixfiveoto.instruction.StackPush;
import com.pjfsw.sixfiveoto.instruction.StackPull;
import com.pjfsw.sixfiveoto.instruction.Rts;
import com.pjfsw.sixfiveoto.instruction.StMemory;
import com.pjfsw.sixfiveoto.instruction.Transfer;
import com.pjfsw.sixfiveoto.instruction.Txb;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Cpu {
    private static final int RESET_VECTOR = 0xFFFC;
    private static final int IRQ_VECTOR = 0xFFFE;
    private static final int NMI_VECTOR = 0xFFFA;

    private final Map<Integer, Instruction> instructions;
    private final AddressDecoder addressDecoder;
    private final Registers registers;
    private long totalCycles = 0;

    private final Map<Integer, String> symbols;

    public Cpu(AddressDecoder addressDecoder, Registers registers) {
        this(addressDecoder, registers, emptyMap());
    }

    public Cpu(AddressDecoder addressDecoder, Registers registers, Map<Integer, String> symbols) {
        this.symbols = symbols;
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
            .put(JmpIndexedIndirect.OPCODE, new JmpIndexedIndirect())
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
            .putAll(Arrays.stream(IncDecRegister.values())
                .collect(toMap(
                    IncDecRegister::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(ModifyFlags.values())
                .collect(toMap(
                    ModifyFlags::opcode,
                    Function.identity())))
            .put(Nop.OPCODE, new Nop())
            .put(Sei.OPCODE, new Sei())
            .put(Cli.OPCODE, new Cli())
            .put(Rti.OPCODE, new Rti())
            .putAll(Arrays.stream(StackPush.values())
                .collect(toMap(
                    StackPush::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(StackPull.values())
                .collect(toMap(
                    StackPull::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(RotateShift.values())
                .collect(toMap(
                    RotateShift::opcode,
                    Function.identity())))
            .put(Rts.OPCODE, new Rts())
            .putAll(Arrays.stream(StMemory.values())
                .collect(toMap(
                    StMemory::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(Transfer.values())
                .collect(toMap(
                    Transfer::opcode,
                    Function.identity())))
            .putAll(Arrays.stream(Txb.values())
                .collect(toMap(
                    Txb::opcode,
                    Function.identity())))
            .build();
        System.out.printf("%d opcodes supported%n", instructions.size());
        reset();
    }

    public void reset() {
        registers.pc = Memory.readWord(addressDecoder, RESET_VECTOR);
        totalCycles = 0;
    }

    public long getCycles() {
        return totalCycles;
    }

    public int getNextOpcode() {
        return addressDecoder.peek(registers.pc);
    }

    public int next() {
        Instruction instruction = instructions.get(addressDecoder.peek(registers.pc));
        registers.incrementPc(1);
        if (instruction != null) {
            int cycles = instruction.execute(registers, addressDecoder, addressDecoder);
            totalCycles += cycles;
            return cycles;
        } else {
            return 0;
        }
    }

    public int irq() {
        if (registers.i()) {
            return 0;
        }
        registers.push(addressDecoder, registers.pc >> 8);
        registers.push(addressDecoder, registers.pc & 0xFF);
        registers.push(addressDecoder, registers.sr());
        registers.pc = Memory.readWord(addressDecoder, IRQ_VECTOR);
        registers.i(true);

        return 7;
    }

    public Disassembler createDisassembler() {
        return new Disassembler(addressDecoder, instructions, symbols, registers.pc);
    }
}
