package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class StaTest {
    private static final int VALUE = 17;

    @Test
    public void testAbsolute() {
        Workbench wb = new Workbench(new Sta.Absolute().assemble(0x0200));
        wb.registers().a(VALUE);
        assertEquals(4,wb.run(1));
        assertEquals(VALUE, wb.peek(0x0200));
    }

    @Test
    public void testAbsoluteX() {
        int offset = 2;
        Workbench wb = new Workbench(new Sta.AbsoluteX().assemble(0x0200));
        wb.registers().a(VALUE);
        wb.registers().x(offset);
        assertEquals(5,wb.run(1));
        assertEquals(VALUE, wb.peek(0x0200 + offset));
    }

}
