package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class StaTest {
    private static final int VALUE = 17;

    @Test
    public void testStaAbsolute() {
        int addr = 0x200;
        Workbench wb = new Workbench(StMemory.STA.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().a(VALUE);
        assertEquals(4,wb.run(1));
        assertEquals(VALUE, wb.peek(addr));
    }

    @Test
    public void testStxAbsolute() {
        int addr = 0x200;
        Workbench wb = new Workbench(StMemory.STX.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().x(VALUE);
        assertEquals(4,wb.run(1));
        assertEquals(VALUE, wb.peek(addr));
    }

    @Test
    public void testStyAbsolute() {
        int addr = 0x200;
        Workbench wb = new Workbench(StMemory.STY.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().y(VALUE);
        assertEquals(4,wb.run(1));
        assertEquals(VALUE, wb.peek(addr));
    }

    @Test
    public void testStaAbsoluteX() {
        int offset = 2;
        int addr = 0x200;
        Workbench wb = new Workbench(StMemory.STAX.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().a(VALUE);
        wb.registers().x(offset);
        assertEquals(5,wb.run(1));
        assertEquals(VALUE, wb.peek(addr + offset));
    }


    @Test
    public void testStaAbsoluteY() {
        int offset = 2;
        int addr = 0x200;
        Workbench wb = new Workbench(StMemory.STAY.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().a(VALUE);
        wb.registers().y(offset);
        assertEquals(5,wb.run(1));
        assertEquals(VALUE, wb.peek(addr + offset));
    }

    @Test
    public void testStaZeroPage() {
        int addr = 0x10;
        Workbench wb = new Workbench(StMemory.STAZ.opcode(), Word.lo(addr));
        wb.registers().a(VALUE);
        assertEquals(3,wb.run(1));
        assertEquals(VALUE, wb.peek(addr));
    }
    @Test
    public void testStxZeroPage() {
        int addr = 0x10;
        Workbench wb = new Workbench(StMemory.STXZ.opcode(), Word.lo(addr));
        wb.registers().x(VALUE);
        assertEquals(3,wb.run(1));
        assertEquals(VALUE, wb.peek(addr));
    }
    @Test
    public void testStyZeroPage() {
        int addr = 0x10;
        Workbench wb = new Workbench(StMemory.STYZ.opcode(), Word.lo(addr));
        wb.registers().y(VALUE);
        assertEquals(3, wb.run(1));
        assertEquals(VALUE, wb.peek(addr));
    }

    @Test
    public void testStaZeroPageIndexed() {
        int addr = 0x10;
        int offset = 2;
        Workbench wb = new Workbench(StMemory.STAZX.opcode(), Word.lo(addr));
        wb.registers().a(VALUE);
        wb.registers().x(2);
        assertEquals(4, wb.run(1));
        assertEquals(VALUE, wb.peek(addr+offset));
    }

    @Test
    public void testIndexedIndirect() {
        int zp = 0x10;
        int addr = 0x200;
        int offset = 2;
        Workbench wb = new Workbench(StMemory.STAIX.opcode(), Word.lo(zp));
        wb.poke(zp+offset, Word.lo(addr));
        wb.poke(zp+offset+1, Word.hi(addr));
        wb.registers().a(VALUE);
        wb.registers().x(offset);
        assertEquals(6, wb.run(1));
        assertEquals(VALUE, wb.peek(addr));
    }

    @Test
    public void testIndirectIndexed() {
        int zp = 0x10;
        int addr = 0x200;
        int offset = 2;
        Workbench wb = new Workbench(StMemory.STAIY.opcode(), Word.lo(zp));
        wb.poke(zp, Word.lo(addr));
        wb.poke(zp+1,Word.hi(addr));

        wb.registers().a(VALUE);
        wb.registers().y(offset);
        assertEquals(6, wb.run(1));
        assertEquals(VALUE, wb.peek(addr+offset));
    }

    @Test
    public void testStz() {
        int addr = 0x200;
        Workbench wb = new Workbench(StMemory.STZ.opcode(), Word.lo(addr), Word.hi(addr));
        wb.registers().a(255);
        assertEquals(4,wb.run(1));
        assertEquals(0, wb.peek(addr));
    }

}
