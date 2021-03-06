package com.pjfsw.sixfiveoto;

import com.pjfsw.sixfiveoto.addressables.Peeker;

public class Memory {
    private Memory() {
    }

    public static Integer readWord(Peeker peek, Integer address) {
        return peek.peek(address) | (peek.peek(address+1) << 8);
    }

    public static int add(int address, int offset) {
        return (address + offset) & 0xFFFF;
    }

    public static int addSigned(int address, int offset) {
        return (address + (byte)offset) & 0xFFFF;
    }

    public static int penalty(int address1, int address2) {
        return (address1 >> 8) != (address2 >> 8) ? 1 : 0;
    }

    public static String format(Integer address) {
        return "$" + String.format("%04X", address);
    }

}
