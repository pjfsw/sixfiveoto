package com.pjfsw.sixfiveoto.instruction;

import com.pjfsw.sixfiveoto.registers.Registers;

public interface LoadFunction {
    int compute(Registers registers, int value);
}
