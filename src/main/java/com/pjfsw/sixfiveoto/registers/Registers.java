package com.pjfsw.sixfiveoto.registers;

public class Registers {
    public int pc;
    public int a;
    public int x;

    public void incrementPc(int steps) {
        pc = (pc + steps) & 0XFFFF;
    }

    public int add(int a, int b) {
        return (a + b) & 0xFF;
    }

    public int subtract(int a, int b) {
        return (a - b) & 0xFF;
    }

    @Override
    public String toString() {
        return String.format("A: $%02X X: $%02X PC: $%04X", a, x, pc);
    }
}
