package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.mnemonicformatter.MnemonicFormatter;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum AddressingMode {
    IMPLIED(AddressingMode::getImplied, AddressingMode::getNoPenalty, 0, MnemonicFormatter.IMPLIED),

    // #$aa
    IMMEDIATE(AddressingMode::getImmediate, AddressingMode::getNoPenalty, 1, MnemonicFormatter.IMMEDIATE),

    // $aaaa
    ABSOLUTE(AddressingMode::getAbsolute, AddressingMode::getNoPenalty, 2, MnemonicFormatter.ABSOLUTE),

    // $aaaa,X
    INDEXED_X(AddressingMode::getIndexedX, AddressingMode::getIndexedXPenalty, 2, MnemonicFormatter.INDEXED_X),

    // $aaaa,Y
    INDEXED_Y(AddressingMode::getIndexedY, AddressingMode::getIndexedYPenalty, 2, MnemonicFormatter.INDEXED_Y),

    // $aa
    ZEROPAGE(AddressingMode::getZeropage, AddressingMode::getNoPenalty, 1,
        MnemonicFormatter.ZEROPAGE),

    // $aa,x
    ZEROPAGE_INDEXED_X(AddressingMode::getZeropageIndexedX, AddressingMode::getNoPenalty, 1,
        MnemonicFormatter.ZEROPAGE_INDEXED_X),

    // $aa,y
    ZEROPAGE_INDEXED_Y(AddressingMode::getZeropageIndexedY, AddressingMode::getNoPenalty, 1,
        MnemonicFormatter.ZEROPAGE_INDEXED_Y),

    // ($aa,x)
    INDEXED_INDIRECT(AddressingMode::getIndexedIndirect, AddressingMode::getNoPenalty, 1,
        MnemonicFormatter.INDEXED_INDIRECT),

    // ($aa),y
    INDIRECT_INDEXED(AddressingMode::getIndirectIndexed, AddressingMode::getIndirectIndexedPenalty, 1, MnemonicFormatter.INDIRECT_INDEXED),

    // ($aa)  65C02 addressing mode
    INDIRECT(AddressingMode::getIndirect, AddressingMode::getNoPenalty, 1, MnemonicFormatter.INDIRECT);

    private final EffectiveAddressGetter addressingMode;
    private final EffectiveAddressGetter penalty;
    private final int parameterSize;
    private final MnemonicFormatter formatter;

    AddressingMode(EffectiveAddressGetter addressingMode,
        EffectiveAddressGetter penalty, int parameterSize,
        MnemonicFormatter formatter) {
        this.addressingMode = addressingMode;
        this.penalty = penalty;
        this.parameterSize = parameterSize;
        this.formatter = formatter;
    }

    int getParameterSize() {
        return parameterSize;
    }

    int getEffectiveAddress(final Registers registers, final Peeker peeker) {
        return addressingMode.get(peeker, registers);
    }

    int getPenalty(final Registers registers, final Peeker peeker) {
        return penalty.get(peeker, registers);

    }
    private static int readZpWord(Peeker peek, int address) {
        return peek.peek(address & 0xFF) | (peek.peek((address+1)&0xFF) << 8);
    }

    public MnemonicFormatter getFormatter() {
        return formatter;
    }

    public interface EffectiveAddressGetter {
        int get(Peeker peeker, Registers registers);
    }

    private static int getNoPenalty(Peeker peeker, Registers registers) {
        return 0;
    }

    private static int getImplied(Peeker peeker, Registers registers) {
        return registers.pc;
    }


    private static int getImmediate(Peeker peeker, Registers registers) {
        return registers.pc;
    }

    private static int getAbsolute(Peeker peeker, Registers registers) {
        return Memory.readWord(peeker, registers.pc);
    }

    private static int getIndexedX(Peeker peeker, Registers registers) {
        int baseAddress = Memory.readWord(peeker, registers.pc);
        return Memory.add(baseAddress, registers.x());
    }

    private static int getIndexedXPenalty(Peeker peeker, Registers registers) {
        int baseAddress = Memory.readWord(peeker, registers.pc);
        return Memory.penalty(baseAddress, Memory.add(baseAddress, registers.x()));
    }

    private static int getIndexedY(Peeker peeker, Registers registers) {
        int baseAddress = Memory.readWord(peeker, registers.pc);
        return Memory.add(baseAddress, registers.y());
    }

    private static int getIndexedYPenalty(Peeker peeker, Registers registers) {
        int baseAddress = Memory.readWord(peeker, registers.pc);
        return Memory.penalty(baseAddress, Memory.add(baseAddress, registers.y()));
    }

    private static int getZeropage(Peeker peeker, Registers registers) {
        return peeker.peek(registers.pc);
    }

    private static int getZeropageIndexedX(Peeker peeker, Registers registers) {
        return Memory.add(peeker.peek(registers.pc), registers.x()) & 0xFF;
    }
    private static int getZeropageIndexedY(Peeker peeker, Registers registers) {
        return Memory.add(peeker.peek(registers.pc), registers.y()) & 0xFF;
    }
    private static int getIndexedIndirect(Peeker peeker, Registers registers) {
        int pointerAddress = Memory.add(peeker.peek(registers.pc), registers.x()) & 0xFF;
        return readZpWord(peeker, pointerAddress);
    }
    private static int getIndirectIndexed(Peeker peeker, Registers registers) {
        int targetAddress = readZpWord(peeker, peeker.peek(registers.pc));
        return Memory.add(targetAddress, registers.y());
    }

    private static int getIndirectIndexedPenalty(Peeker peeker, Registers registers) {
        int baseAddress = readZpWord(peeker, peeker.peek(registers.pc));
        int offsetAddress = Memory.add(baseAddress, registers.y());

        return  Memory.penalty(baseAddress, offsetAddress);
    }

    private static int getIndirect(Peeker peeker, Registers registers) {
        return readZpWord(peeker, peeker.peek(registers.pc));
    }



}
