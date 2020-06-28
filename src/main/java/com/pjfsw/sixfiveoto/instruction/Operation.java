package com.pjfsw.sixfiveoto.instruction;

import java.util.function.BiFunction;

import com.pjfsw.sixfiveoto.registers.Registers;

public enum Operation implements BiFunction<Registers, Integer, Integer> {
    EQU((reg,nw)->(nw)),
    AND((reg,nw)->(reg.a() & nw)),
    ORA((reg,nw)->(reg.a() | nw)),
    EOR((reg,nw)->(reg.a() ^ nw))
    ;

    private final BiFunction<Registers, Integer, Integer> function;

    Operation(BiFunction<Registers, Integer, Integer> function) {
        this.function = function;
    }

    @Override
    public Integer apply(final Registers registers, final Integer integer) {
        return function.apply(registers, integer);
    }
}
