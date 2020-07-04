package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class RotateShiftTest {
    @Test
    public void testAsl() {
        Workbench wb = new Workbench(RotateShift.ASLI.opcode());
        wb.registers().a(0xc0);
        wb.registers().c = false;
        assertEquals(2, wb.run(1));
        assertTrue(wb.registers().c);
        assertEquals(0x80, wb.registers().a());
    }

    @Test
    public void testLsr() {
        int addr = 0x10;
        Workbench wb = new Workbench(RotateShift.LSRZ.opcode(), addr);
        wb.poke(addr, 0x03);
        wb.registers().c = false;
        assertEquals(5, wb.run(1));
        assertTrue(wb.registers().c);
        assertEquals(0x01, wb.peek(addr));
    }

    @Test
    public void testRol() {
        int addr = 0x0200;
        Workbench wb = new Workbench(RotateShift.ROLA.opcode(), Word.lo(addr), Word.hi(addr));
        wb.poke(addr, 0x01);
        wb.registers().c = true;
        assertEquals(6, wb.run(1));
        assertFalse(wb.registers().c);
        assertEquals(0x03, wb.peek(addr));
    }

    @Test
    public void testRor() {
        Workbench wb = new Workbench(RotateShift.RORI.opcode());
        wb.registers().a(0x81);
        wb.registers().c = false;
        assertEquals(2, wb.run(1));
        assertTrue(wb.registers().c);
        assertEquals(0x40, wb.registers().a());
    }


}
