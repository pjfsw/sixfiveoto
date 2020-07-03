package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Workbench;

public class CmpTest {
    @Test
    void testEquals() {
        Workbench wb = new Workbench(ImmutableList.of(Cmp.CMPI.opcode(), 50));
        wb.registers().a(50);
        wb.run(1);
        assertTrue(wb.registers().c);
        assertTrue(wb.registers().z);
    }

    @Test
    void testGreaterThan() {
        Workbench wb = new Workbench(ImmutableList.of(Cmp.CMPI.opcode(), 40));
        wb.registers().a(50);
        wb.run(1);
        assertTrue(wb.registers().c);
        assertFalse(wb.registers().z);
    }

    @Test
    void testLessThan() {
        Workbench wb = new Workbench(ImmutableList.of(Cmp.CMPI.opcode(), 60));
        wb.registers().a(50);
        wb.run(1);
        assertFalse(wb.registers().c);
        assertFalse(wb.registers().z);
    }

}
