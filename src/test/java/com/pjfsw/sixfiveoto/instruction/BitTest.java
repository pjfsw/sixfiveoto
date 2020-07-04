package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Workbench;

public class BitTest {
    private static final int ADDR = 0x20;
    @Test
    void testZ() {
        Workbench wb = new Workbench(ImmutableList.of(Bit.BITZ.opcode(), ADDR));
        wb.poke(ADDR, 0x0f);
        wb.registers().a(0x30);
        wb.registers().z = false;
        wb.registers().n = false;
        wb.registers().v = false;
        wb.run(1);
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
        assertFalse(wb.registers().v);
    }

    @Test
    void testN() {
        Workbench wb = new Workbench(ImmutableList.of(Bit.BITZ.opcode(), ADDR));
        wb.poke(ADDR, 0x8f);
        wb.registers().a(0x0f);
        wb.registers().z = false;
        wb.registers().n = false;
        wb.registers().v = false;
        wb.run(1);
        assertFalse(wb.registers().z);
        assertTrue(wb.registers().n);
        assertFalse(wb.registers().v);
    }

    @Test
    void testV() {
        Workbench wb = new Workbench(ImmutableList.of(Bit.BITZ.opcode(), ADDR));
        wb.poke(ADDR, 0x4f);
        wb.registers().a(0x4f);
        wb.registers().z = false;
        wb.registers().n = false;
        wb.registers().v = false;
        wb.run(1);
        assertFalse(wb.registers().z);
        assertFalse(wb.registers().n);
        assertTrue(wb.registers().v);
    }

    @Test
    void testImmediateNVUnaffected() {
        //
        int value = 0xf0;
        Workbench wb = new Workbench(ImmutableList.of(Bit.BITI.opcode(), value));
        wb.registers().a(0x0f);
        wb.registers().n = false;
        wb.registers().v = false;
        assertEquals(2,wb.run(1));
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
        assertFalse(wb.registers().v);
    }
}
