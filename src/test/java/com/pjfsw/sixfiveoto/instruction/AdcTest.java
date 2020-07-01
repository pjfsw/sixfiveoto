package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class AdcTest {
    @Test
    public void testAdcImmediate() {
        int value = 0x20;
        Workbench wb = new Workbench(Adc.ADCI.opcode(), value);
        wb.registers().c = false;
        wb.registers().a(0x40);
        assertEquals(2, wb.run(1));
        assertEquals(0x60, wb.registers().a());
        assertFalse(wb.registers().c);
    }

    @Test
    public void testAdcAbsolute() {
        int addr = 0x200;
        Workbench wb = new Workbench(Adc.ADC.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().c = false;
        wb.registers().a(0x40);
        wb.poke(addr, 0xF0);
        wb.run(1);
        assertEquals(0x30, wb.registers().a());
        assertTrue(wb.registers().c);
        assertEquals(4, wb.cycles());
    }

    @Test
    public void testSbcImmediate() {
        int value = 0x20;
        Workbench wb = new Workbench(Adc.SBCI.opcode(), value);
        wb.registers().c = true;
        wb.registers().a(0x40);
        assertEquals(2, wb.run(1));
        assertEquals(0x20, wb.registers().a());
        assertTrue(wb.registers().c);
    }

    @Test
    public void testSbcZeropage() {
        int addr = 0x10;
        Workbench wb = new Workbench(Adc.SBCZ.opcode(), Word.lo(addr), Adc.SBCZ.opcode(), Word.lo(addr));
        wb.registers().c = true;
        wb.registers().a(0x40);
        wb.poke(addr, 0x30); // 0x40 - 0x30 - 0x30 = -0x20 = 0xE0
        assertEquals(3, wb.run(1));
        assertEquals(0x10, wb.registers().a());
        assertTrue(wb.registers().c);
        assertEquals(3, wb.run(1));
        assertEquals(0xE0, wb.registers().a());
        assertFalse(wb.registers().c);
    }


}
