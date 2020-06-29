package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiFunction;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum AddressingMode {
    ZEROPAGE_INDEXED_X((peeker, registers) -> {
        return Memory.add(peeker.peek(registers.pc), registers.x()) & 0xFF;
    }),
    ZEROPAGE_INDEXED_Y((peeker, registers) -> {
        return Memory.add(peeker.peek(registers.pc), registers.y()) & 0xFF;
    }),
    INDEXED_INDIRECT((peeker, registers) -> {
        int pointerAddress = Memory.add(peeker.peek(registers.pc), registers.x()) & 0xFF;
        return Memory.readWord(peeker, pointerAddress);
    });

    private final BiFunction<Peeker, Registers, Integer> addressingMode;

    AddressingMode(BiFunction<Peeker, Registers, Integer> addressingMode) {
        this.addressingMode = addressingMode;
    }

    int getEffectiveAddress(final Registers registers, final Peeker peeker) {
        return addressingMode.apply(peeker, registers);
    }
}
