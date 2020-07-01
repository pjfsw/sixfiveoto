package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.pjfsw.sixfiveoto.registers.Registers;

public enum AluOperation {
    ADC(registers -> registers::adc),
    SBC(registers -> registers::sbc);

    private final Function<Registers, BiFunction<Integer, Integer, Integer>> operation;

    AluOperation(Function<Registers, BiFunction<Integer, Integer, Integer>> operation) {
        this.operation = operation;
    }

    public BiFunction<Integer, Integer, Integer> calculate(Registers registers) {
        return operation.apply(registers);
    }
}
