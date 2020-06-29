package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiFunction;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum AddressingMode {
    ZEROPAGE_INDEXED_X((peeker, registers) -> {
        return Memory.add(peeker.peek(registers.pc), registers.x()) & 0xFF;
    }, (peeker, registers) -> (0)),
    ZEROPAGE_INDEXED_Y((peeker, registers) -> {
        return Memory.add(peeker.peek(registers.pc), registers.y()) & 0xFF;
    }, (peeker, registers) -> (0)),
    INDEXED_INDIRECT((peeker, registers) -> {
        int pointerAddress = Memory.add(peeker.peek(registers.pc), registers.x()) & 0xFF;
        return readZpWord(peeker, pointerAddress);
    }, (peeker, registers) -> (0)),
    INDIRECT_INDEXED((peeker, registers) -> {
        int targetAddress = Memory.readWord(peeker, peeker.peek(registers.pc));
        return Memory.add(targetAddress, registers.y());
    }, (peeker, registers) -> {
        int pointerAddress = peeker.peek(registers.pc);
        return  Memory.penalty(pointerAddress, pointerAddress+1);
        // TODO: What is the penalty? Crossing from ZP to normal??
    });

    private final BiFunction<Peeker, Registers, Integer> addressingMode;
    private final BiFunction<Peeker, Registers, Integer> penalty;

    AddressingMode(BiFunction<Peeker, Registers, Integer> addressingMode,
        BiFunction<Peeker, Registers, Integer> penalty) {
        this.addressingMode = addressingMode;
        this.penalty = penalty;
    }

    int getEffectiveAddress(final Registers registers, final Peeker peeker) {
        return addressingMode.apply(peeker, registers);
    }

    int getPenalty(final Registers registers, final Peeker peeker) {
        return penalty.apply(peeker, registers);

    }
    private static Integer readZpWord(Peeker peek, Integer address) {
        return peek.peek(address & 0xFF) | (peek.peek((address+1)&0xFF) << 8);
    }

}
