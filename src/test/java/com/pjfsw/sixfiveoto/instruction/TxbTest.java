package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class TxbTest {
    private static final int ZADDR = 0x10;
    private static final int ADDR = 0x200;

    @Test
    public void testTrbNonZero() {
        Workbench wb = new Workbench(Txb.TRBZ.opcode(), ZADDR);
        wb.registers().a(0XF0);
        wb.poke(ZADDR, 0xAA);
        assertEquals(5, wb.run(1));
        assertEquals(0x0A, wb.peek(ZADDR));
        assertFalse(wb.registers().z);
    }

    @Test
    public void testTrbZero() {
        Workbench wb = new Workbench(Txb.TRBA.opcode(),  Word.lo(ADDR), Word.hi(ADDR));
        wb.registers().a(0XF0);
        wb.poke(ADDR, 0x0F);
        assertEquals(6, wb.run(1));
        assertEquals(0x0F, wb.peek(ADDR));
        assertTrue(wb.registers().z);
    }

    @Test
    public void testTsbNonZero() {
        Workbench wb = new Workbench(Txb.TSBZ.opcode(), ZADDR);
        wb.registers().a(0XF0);
        wb.poke(ZADDR, 0xAA);
        assertEquals(5, wb.run(1));
        assertEquals(0xFA, wb.peek(ZADDR));
        assertFalse(wb.registers().z);
    }

    @Test
    public void testTsbZero() {
        Workbench wb = new Workbench(Txb.TSBA.opcode(),  Word.lo(ADDR), Word.hi(ADDR));
        wb.registers().a(0XF0);
        wb.poke(ADDR, 0x0F);
        assertEquals(6, wb.run(1));
        assertEquals(0xFF, wb.peek(ADDR));
        assertTrue(wb.registers().z);
    }
}
