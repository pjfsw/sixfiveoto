package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class DexTest {
    @Test
    public void testNormal() {
        Workbench wb = new Workbench(IncDecRegister.DEX.opcode());
        wb.registers().x(0x1);
        assertEquals(2,wb.run(1));
        assertEquals(0,wb.registers().x());
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
    }

    @Test
    public void testNegative() {
        Workbench wb = new Workbench(IncDecRegister.DEX.opcode());
        wb.registers().x(0x00);
        assertEquals(2,wb.run(1));
        assertEquals(0xFF,wb.registers().x());
        assertFalse(wb.registers().z);
        assertTrue(wb.registers().n);
    }
}
