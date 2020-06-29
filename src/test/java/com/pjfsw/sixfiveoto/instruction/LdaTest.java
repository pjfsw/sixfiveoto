package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Workbench;

public class LdaTest {
    private static final int POSITIVE = 17;
    private static final int ZERO = 0;
    private static final int NEGATIVE = 0xF0;

    @Test
    public void testImmediate() {
        Workbench wb = new Workbench(ImmutableList.<Integer>builder()
            .addAll(LdImmediate.LDA.assemble(POSITIVE))
            .addAll(LdImmediate.LDA.assemble(NEGATIVE))
            .addAll(LdImmediate.LDA.assemble(ZERO))
            .build());
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
        Workbench wb = new Workbench(LdImmediate.AND.assemble(0x0f));
        wb.registers().a(0xf8);
        wb.run(1);
        assertEquals(0x08, wb.registers().a());
    }

    @Test
    public void testOr() {
        Workbench wb = new Workbench(LdImmediate.ORA.assemble(0x0f));
        wb.registers().a(0xf8);
        wb.run(1);
        assertEquals(0xff, wb.registers().a());
    }

    @Test
    public void testEor() {
        Workbench wb = new Workbench(LdImmediate.EOR.assemble(0x0f));
        wb.registers().a(0x55);
        wb.run(1);
        assertEquals(0x5a, wb.registers().a());
    }

    @Test
    public void testAbsolute() {
        Workbench wb = new Workbench(LdAbsolute.LDA.assemble(0x0200));
        wb.poke(0x0200, POSITIVE);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(4, wb.cycles());
    }

    @Test
    public void testZeroPage() {
        Workbench wb = new Workbench(LdZeroPage.LDA.assemble(0x20));
        wb.poke(0x20, POSITIVE);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(3, wb.cycles());
    }


    @Test
    public void testAbsoluteX() {
        Workbench wb = new Workbench(LdIndexed.LDAX.assemble(0x0200));
        wb.poke(0x0202, POSITIVE);
        wb.registers().x(2);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(4, wb.cycles());
    }


    @Test
    public void testAbsoluteYWithPenalty() {
        Workbench wb = new Workbench(LdIndexed.LDAY.assemble(0x02FF));
        wb.poke(0x0301, POSITIVE);
        wb.registers().y(2);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(5, wb.cycles());
    }

    @Test
    public void testZeroPageX() {
        Workbench wb = new Workbench(LdZeroPageIndexed.LDAX.assemble(0x20));
        wb.poke(0x22, POSITIVE);
        wb.registers().x(2);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().a());
        assertEquals(4, wb.cycles());
    }

    @Test
    public void testZeroPageY() {
        Workbench wb = new Workbench(LdZeroPageIndexed.LDXY.assemble(0x20));
        wb.poke(0x22, POSITIVE);
        wb.registers().y(2);
        wb.run(1);
        assertEquals(POSITIVE, wb.registers().x());
        assertEquals(4, wb.cycles());
    }


    @Test
    public void testIndexedIndirect() {
        Workbench wb = new Workbench(LdIndexedIndirect.LDAX.assemble(0xfe));
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
}
