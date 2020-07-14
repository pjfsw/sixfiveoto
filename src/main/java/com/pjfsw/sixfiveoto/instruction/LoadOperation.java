package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiFunction;

import com.pjfsw.sixfiveoto.registers.Registers;

public enum LoadOperation {
    LD(LoadOperation::load),
    AND(LoadOperation::and),
    ORA(LoadOperation::or),
    EOR(LoadOperation::eor)
    ;

    private final LoadFunction function;

    LoadOperation(LoadFunction function) {
        this.function = function;
    }

    public int compute(final Registers registers, final int integer) {
        return function.compute(registers, integer);
    }

    private static int load(Registers registers, int value) {
        return value;
    }

    private static int and(Registers registers, int value) {
        return registers.a() & value;
    }

    private static int or(Registers registers, int value) {
        return registers.a() | value;
    }

    private static int eor(Registers registers, int value) {
        return registers.a() ^ value;
    }

}
