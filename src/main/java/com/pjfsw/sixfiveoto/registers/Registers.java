package com.pjfsw.sixfiveoto.registers;

public class Registers {
    public int pc;

    public void incrementPc(int steps) {
        pc = (pc + steps) & 0XFFFF;
    }
}
