package com.pjfsw.sixfiveoto.instruction;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class LdaTest {
    private static final int POSITIVE = 17;
    private static final int ZERO = 0;
    private static final int NEGATIVE = 0xF0;

    @Test
    public void testImmediate() {
        Workbench wb = new Workbench(
            LdImmediate.LDA.opcode(), POSITIVE,
            LdImmediate.LDA.opcode(), NEGATIVE,
            LdImmediate.LDA.opcode(), ZERO);

        assertEquals(2, wb.run(1));
        assertEquals(POSITIVE, wb.registers().a());
        assertFalse(wb.registers().z);
        assertFalse(wb.registers().n);
        assertEquals(2, wb.run(1));
        assertEquals(NEGATIVE, wb.registers().a());
        assertFalse(wb.registers().z);
        assertTrue(wb.registers().n);
        assertEquals(2, wb.run(1));
        assertEquals(ZERO, wb.registers().a());
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
    }

    @Test
    public void testAnd() {
        Workbench wb = new Workbench(LdImmediate.AND.opcode(), 0x0f);
        wb.registers().a(0xf8);
        wb.run(1);
        assertEquals(0x08, wb.registers().a());
    }

    @Test
    public void testOr() {
        Workbench wb = new Workbench(LdImmediate.ORA.opcode(), 0x0f);
        wb.registers().a(0xf8);
        wb.run(1);
        assertEquals(0xff, wb.registers().a());
    }

    @Test
    public void testEor() {
        Workbench wb = new Workbench(LdImmediate.EOR.opcode(), 0x0f);
        wb.registers().a(0x55);
        wb.run(1);
        assertEquals(0x5a, wb.registers().a());
    }

    @Test
    public void testAbsolute() {
        int addr = 0x200;
        Workbench wb = new Workbench(0xAD, Word.lo(addr), Word.hi(addr));
        wb.poke(addr, POSITIVE);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(4, wb.cycles());
    }

    @Test
    public void testZeroPage() {
        Workbench wb = new Workbench(LdZeroPage.LDA.opcode(), 0x20);
        wb.poke(0x20, POSITIVE);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(3, wb.cycles());
    }

    @Test
    public void testAbsoluteX() {
        int addr = 0x200;
        Workbench wb = new Workbench(0xBD, Word.lo(addr), Word.hi(addr));
        wb.poke(addr+2, POSITIVE);
        wb.registers().x(2);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(4, wb.cycles());
    }


    @Test
    public void testAbsoluteYWithPenalty() {
        int addr = 0x2ff;
        Workbench wb = new Workbench(0xB9, Word.lo(addr), Word.hi(addr));
        wb.poke(addr+2, POSITIVE);
        wb.registers().y(2);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(5, wb.cycles());
    }

    @Test
    public void testZeroPageX() {
        Workbench wb = new Workbench(LdZeroPageIndexed.LDAX.opcode(), 0x20);
        wb.poke(0x22, POSITIVE);
        wb.registers().x(2);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(4, wb.cycles());
    }

    @Test
    public void testZeroPageY() {
        Workbench wb = new Workbench(LdZeroPageIndexed.LDXY.opcode(), 0x20);
        wb.poke(0x22, POSITIVE);
        wb.registers().y(2);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().x());
        assertEquals(4, wb.cycles());
    }


    @Test
    public void testIndexedIndirect() {
        Workbench wb = new Workbench(LdIndexedIndirect.LDAX.opcode(), 0xfe);
        // value
        wb.poke(0x0123, POSITIVE);
        // pointer to 0x0123
        wb.poke(0xff, 0x23);
        wb.poke(0x00, 0x01);
        // offset
        wb.registers().x(1);
        assertEquals(6,wb.run(1));
        assertEquals(POSITIVE, wb.registers().a());
    }

    @Test
    public void testIndirectIndexed() {
        Workbench wb = new Workbench(LdIndirectIndexed.LDAY.opcode(), 0xff);
        // value
        wb.poke(0x0124, POSITIVE);
        // pointer to 0x0123
        wb.poke(0xff, 0x23);
        wb.poke(0x00, 0x01);
        // offset
        wb.registers().y(1);
        assertEquals(5,wb.run(1));
        assertEquals(POSITIVE, wb.registers().a());
    }

    @Test
    public void testIndirectIndexedPenalty() {
        Workbench wb = new Workbench(LdIndirectIndexed.LDAY.opcode(), 0xff);
        // value
        wb.poke(0x0200, POSITIVE);
        // pointer to 0x0123
        wb.poke(0xff, 0x80);
        wb.poke(0x00, 0x01);
        // offset
        wb.registers().y(0x80);
        assertEquals(6,wb.run(1));
        assertEquals(POSITIVE, wb.registers().a());
    }

}
