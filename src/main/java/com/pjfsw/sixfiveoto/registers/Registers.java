package com.pjfsw.sixfiveoto.registers;

public class Registers {
    public int pc;
    private int a;
    private int x;
    public boolean z;
    public boolean n;

    public void incrementPc(int steps) {
        pc = (pc + steps) & 0XFFFF;
    }

    private void setFlags(int a) {
        z = a == 0;
        n = (a & 0x80) != 0;
    }

    public int add(int a, int b) {
        int result = (a + b) & 0xFF;
        setFlags(result);
        return result;
    }

    @Override
    public String toString() {
        return String.format("A: $%02X X: $%02X %s%s", a, x, n ? "N" : ".", z ? "Z" : ".");
    }

    public void a(final int a) {
        setFlags(a);
        this.a = a;
    }

    public int a() {
        return this.a;
    }

    public void x(final int x) {
        setFlags(x);
        this.x = x;
    }

    public int x() {
        return this.x;
    }
}
