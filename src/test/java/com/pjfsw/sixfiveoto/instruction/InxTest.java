package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class InxTest {
    @Test
    public void testNormal() {
        Workbench wb = new Workbench(IncDecXY.INX.assemble(0));
        wb.run(1);
        assertEquals(1,wb.registers().x());
        assertEquals(2, wb.cycles());
        assertFalse(wb.registers().z);
        assertFalse(wb.registers().n);
    }

    @Test
    public void testNegative() {
        Workbench wb = new Workbench(IncDecXY.INX.assemble(0));
        wb.registers().x(0x7F);
        wb.run(1);
        assertEquals(0x80,wb.registers().x());
        assertEquals(2, wb.cycles());
        assertFalse(wb.registers().z);
        assertTrue(wb.registers().n);
    }

    @Test
    public void testWrap() {
        Workbench wb = new Workbench(IncDecXY.INX.assemble(0));
        wb.registers().x(255);
        wb.run(1);
        assertEquals(0, wb.registers().x());
        assertEquals(2, wb.cycles());
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
    }
}