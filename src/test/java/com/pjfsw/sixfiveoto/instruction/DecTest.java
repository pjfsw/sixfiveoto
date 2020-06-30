package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class DecTest {
    @Test
    public void testNormal() {
        Workbench wb = new Workbench(ImmutableList.of(IncDec.DEC.opcode(), Word.lo(0x0200), Word.hi(0x200)));
        wb.poke(0x200, 1);
        assertEquals(6, wb.run(1));
        assertEquals(0, wb.peek(0x200));
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
    }

    @Test
    public void testNormalIndexed() {
        Workbench wb = new Workbench(ImmutableList.of(IncDec.DECX.opcode(), Word.lo(0x0200), Word.hi(0x200)));
        wb.poke(0x202, 1);
        wb.registers().x(2);
        assertEquals(7, wb.run(1));
        assertEquals(0, wb.peek(0x202));
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
    }


    @Test
    public void testNegative() {
        Workbench wb = new Workbench(ImmutableList.of(IncDec.DEC.opcode(), Word.lo(0x0200), Word.hi(0x200)));
        wb.poke(0x200, 0);
        assertEquals(6, wb.run(1));
        assertEquals(0xFF, wb.peek(0x200));
        assertFalse(wb.registers().z);
        assertTrue(wb.registers().n);
    }
}
