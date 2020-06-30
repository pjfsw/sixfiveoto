package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiFunction;

import com.pjfsw.sixfiveoto.Memory;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.registers.Registers;

public enum AddressingMode {
    // $aaaa
    ABSOLUTE((peeker, registers) -> Memory.readWord(peeker, registers.pc),(peeker, registers) -> (0)),

    // $aaaa,X
    INDEXED_X((peeker, registers) -> {
        int baseAddress = Memory.readWord(peeker, registers.pc);
        return Memory.add(baseAddress, registers.x());

    }, (peeker, registers) -> {
        int baseAddress = Memory.readWord(peeker, registers.pc);
        return Memory.penalty(baseAddress, Memory.add(baseAddress, registers.x()));
    }),

    // $aaaa,Y
    INDEXED_Y((peeker, registers) -> {
        int baseAddress = Memory.readWord(peeker, registers.pc);
        return Memory.add(baseAddress, registers.y());

    }, (peeker, registers) -> {
        int baseAddress = Memory.readWord(peeker, registers.pc);
        return Memory.penalty(baseAddress, Memory.add(baseAddress, registers.y()));
    }),

    // $aa
    ZEROPAGE((peeker, registers) -> peeker.peek(registers.pc), (peeker, registers) -> (0)),

    // $aa,x
    ZEROPAGE_INDEXED_X((peeker, registers) ->
        Memory.add(peeker.peek(registers.pc), registers.x()) & 0xFF, (peeker, registers) -> (0)),

    // $aa,y
    ZEROPAGE_INDEXED_Y((peeker, registers) ->
        Memory.add(peeker.peek(registers.pc), registers.y()) & 0xFF, (peeker, registers) -> (0)),

    // ($aa,x)
    INDEXED_INDIRECT((peeker, registers) -> {
        int pointerAddress = Memory.add(peeker.peek(registers.pc), registers.x()) & 0xFF;
        return readZpWord(peeker, pointerAddress);
    }, (peeker, registers) -> (0)),

    // ($aa),y
    INDIRECT_INDEXED((peeker, registers) -> {
        int targetAddress = readZpWord(peeker, peeker.peek(registers.pc));
        return Memory.add(targetAddress, registers.y());
    }, (peeker, registers) -> {
        int baseAddress = readZpWord(peeker, peeker.peek(registers.pc));
        int offsetAddress = Memory.add(baseAddress, registers.y());

        return  Memory.penalty(baseAddress, offsetAddress);
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
