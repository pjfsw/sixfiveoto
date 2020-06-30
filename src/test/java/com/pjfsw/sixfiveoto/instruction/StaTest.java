package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class StaTest {
    private static final int VALUE = 17;

    @Test
    public void testAbsolute() {
        int addr = 0x200;
        Workbench wb = new Workbench(StAbsolute.STA.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().a(VALUE);
        assertEquals(4,wb.run(1));
        assertEquals(VALUE, wb.peek(addr));
    }

    @Test
    public void testAbsoluteX() {
        int offset = 2;
        int addr = 0x200;
        Workbench wb = new Workbench(StIndexed.STAX.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().a(VALUE);
        wb.registers().x(offset);
        assertEquals(5,wb.run(1));
        assertEquals(VALUE, wb.peek(addr + offset));
    }


    @Test
    public void testAbsoluteY() {
        int offset = 2;
        int addr = 0x200;
        Workbench wb = new Workbench(StIndexed.STAY.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().a(VALUE);
        wb.registers().y(offset);
        assertEquals(5,wb.run(1));
        assertEquals(VALUE, wb.peek(addr + offset));
    }

}
