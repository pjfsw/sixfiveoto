package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class IncTest {
    @Test
    public void testNormal() {
        Workbench wb = new Workbench(ImmutableList.of(IncDec.INC.opcode(), Word.lo(0x0200), Word.hi(0x200)));
        wb.poke(0x200, 0);
        assertEquals(6, wb.run(1));
        assertEquals(1, wb.peek(0x200));
        assertFalse(wb.registers().z);
        assertFalse(wb.registers().n);
    }

    @Test
    public void testNormalIndexed() {
        Workbench wb = new Workbench(ImmutableList.of(IncDec.INCX.opcode(), Word.lo(0x0200), Word.hi(0x200)));
        wb.registers().x(2);
        wb.poke(0x202, 0);
        assertEquals(7, wb.run(1));
        assertEquals(1, wb.peek(0x202));
        assertFalse(wb.registers().z);
        assertFalse(wb.registers().n);
    }

    @Test
    public void testNegative() {
        Workbench wb = new Workbench(ImmutableList.of(IncDec.INC.opcode(), Word.lo(0x0200), Word.hi(0x200)));
        wb.poke(0x200, 0x7F);
        assertEquals(6, wb.run(1));
        assertEquals(0x80, wb.peek(0x200));
        assertFalse(wb.registers().z);
        assertTrue(wb.registers().n);
    }

    @Test
    public void testWrap() {
        Workbench wb = new Workbench(ImmutableList.of(IncDec.INC.opcode(), Word.lo(0x0200), Word.hi(0x200)));
        wb.poke(0x200, 0xFF);
        assertEquals(6, wb.run(1));
        assertEquals(0x00, wb.peek(0x200));
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
    }
}
