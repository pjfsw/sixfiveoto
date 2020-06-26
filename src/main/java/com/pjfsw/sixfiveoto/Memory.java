package com.pjfsw.sixfiveoto;

import java.util.function.Function;

public class Memory {
    private Memory() {
    }
    public static Integer read16Bit(Function<Integer,Integer> peek, Integer address) {
        return peek.apply(address) | (peek.apply(address+1) << 8);
    }

    public static String format(Integer address) {
        return "$" + String.format("%04X", address);
    }

}
