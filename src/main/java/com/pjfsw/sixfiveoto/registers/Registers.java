package com.pjfsw.sixfiveoto.registers;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;

public class Registers {
    public int pc;
    private int a;
    private int x;
    private int y;
    public int sp;
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

    public int and(int a, int b) {
        int result = a & b;
        setFlags(result);
        return result;
    }

    public int or(int a, int b) {
        int result = a | b;
        setFlags(result);
        return result;
    }

    public int eor(int a, int b) {
        int result = a ^ b;
        setFlags(result);
        return result;
    }

    @Override
    public String toString() {
        return String.format("A: $%02X X: $%02X Y: $%02X %s%s", a, x, y, n ? "N" : ".", z ? "Z" : ".");
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

    public void y(final int y) {
        setFlags(y);
        this.y = y;
    }

    public int y() {
        return this.y;
    }


    public int sp() {
        return this.sp;
    }

    public void sp(int x) {
        this.sp = x;
    }

    public int pull(Peeker peeker) {
        sp = (sp + 1) & 0xFF;
        return peeker.peek(0x100 + sp);
    }

    public void push(Poker poker, int value) {
        poker.poke(0x100 + sp, value);
        sp = (sp - 1) & 0xFF;
    }
}
