package com.pjfsw.sixfiveoto.registers;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;

public class Registers {
    // STATUS REGISTER: nv-bdizc
    public int pc;
    private int a;
    private int x;
    private int y;
    public boolean c;
    public int sp;
    public boolean v;
    public boolean z;
    public boolean n;
    private boolean i;

    private static final int N_MASK = 0x80;
    private static final int V_MASK = 0x40;
    private static final int I_MASK = 0x04;
    private static final int Z_MASK = 0x02;
    private static final int C_MASK = 0x01;

    public void incrementPc(int steps) {
        pc = (pc + steps) & 0XFFFF;
    }

    private void setFlags(int a) {
        z = a == 0;
        n = (a & 0x80) != 0;
    }

    private void setCarry(int a) {
        c = (a & 0x100) == 0x100;
    }

    public int add(int a, int b) {
        int result = (a + b) & 0xFF;
        setFlags(result);
        return result;
    }

    public int adc(int a, int b) {
        int e = a + b + (c ? 1 : 0);
        setCarry(e);
        e = e & 0xFF;
        v = ((a^e)  & (b^e) & 0x80) != 0;
        setFlags(e);
        return e;
    }

    public int sbc(int a, int b) {
        int binv = b ^ 0xFF;
        int cadd = c ? 1 : 0;
        int e = a + binv + cadd;
        setCarry(e);
        v = ((a^e)  & ((255-b)^e) & 0x80) != 0;
        e = e & 0xFF;
        setFlags(e);
        return e;
    }

    public void and(int a, int b) {
        int result = a & b;
        setFlags(result);
    }

    public int asl(int a) {
        c = (a & 0x80) != 0;
        a = (a << 1) & 0xFF;
        setFlags(a);
        return a;
    }

    public int lsr(int a) {
        c = (a & 0x01) != 0;
        a = a >> 1;
        setFlags(a);
        return a;
    }

    public int rol(int a) {
        int ci = c ? 0x01 : 0x00;
        c = (a & 0x80) != 0;
        a = ((a << 1) | ci) & 0xFF;
        setFlags(a);

        return a;
    }

    public int ror(int a) {
        int ci = c ? 0x80 : 0x00;
        c = (a & 0x01) != 0;
        a = (a >> 1) | ci;
        setFlags(a);
        return a;
    }



    @Override
    public String toString() {
        return String.format("A: $%02X X: $%02X Y: $%02X %c%c....%c%c", a, x, y,
            n ? 'N' : '.',
            v ? 'V' : '.',
            z ? 'Z' : '.',
            c ? 'C' : '.');
    }
    public void sr(final int regs) {
        n = (regs & N_MASK) != 0;
        v = (regs & V_MASK) != 0;
        i = (regs & I_MASK) != 0;
        z = (regs & Z_MASK) != 0;
        c = (regs & C_MASK) != 0;
    }

    public int sr() {
        return (n ? N_MASK : 0)
            | (v ? V_MASK : 0)
            | (i ? I_MASK : 0)
            | (z ? Z_MASK : 0)
            | (c ? C_MASK : 0);
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

    public void i(boolean i) {
        this.i = i;
    }

    public boolean i() {
        return i;
    }
}
