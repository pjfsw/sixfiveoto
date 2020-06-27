package com.pjfsw.sixfiveoto;

import com.pjfsw.sixfiveoto.addressables.Peeker;

public class Memory {
    private Memory() {
    }

    public static Integer read16Bit(Peeker peek, Integer address) {
        return peek.peek(address) | (peek.peek(address+1) << 8);
    }

    public static int add(int address1, int address2) {
        return (address1 + address2) & 0xFFFF;
    }

    public static int penalty(int address1, int address2) {
        return Math.abs(((address1 + address2) >> 8) - (address1 >> 8));
    }

    public static String format(Integer address) {
        return "$" + String.format("%04X", address);
    }

}
