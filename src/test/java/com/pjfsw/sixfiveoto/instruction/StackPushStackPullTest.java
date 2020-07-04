package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class StackPushStackPullTest {
    @Test
    public void testPha() {
        Workbench wb = new Workbench(StackPush.PHA.opcode(), StackPull.PLA.opcode());
        wb.registers().a(17);
        assertEquals(3, wb.run(1)); // PHA
        wb.registers().a(99);
        assertEquals(4, wb.run(1)); // PLA
        assertEquals(17, wb.registers().a());
    }

    @Test
    public void testPhp() {
        Workbench wb = new Workbench(StackPush.PHP.opcode(), StackPull.PLP.opcode());
        wb.registers().c = false;
        wb.registers().z = true;
        wb.registers().v = false;
        wb.registers().n = true;
        assertEquals(3, wb.run(1)); // PHA
        wb.registers().c = true;
        wb.registers().z = false;
        wb.registers().v = true;
        wb.registers().n = false;
        assertEquals(4, wb.run(1)); // PLA
        assertFalse(wb.registers().c);
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().v);
        assertTrue(wb.registers().n);
    }

}
