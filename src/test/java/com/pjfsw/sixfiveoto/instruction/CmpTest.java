package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
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

    @Test
    void testCpxAbsolute1() {
        int addr = 0x200;
        Workbench wb = new Workbench(ImmutableList.of(Cmp.CPXA.opcode(), Word.lo(addr), Word.hi(addr)));
        wb.registers().x(0x80);
        wb.poke(addr, 0x81);
        wb.run(1);
        assertFalse(wb.registers().c);
    }
    @Test
    void testCpxAbsolute0() {
        int addr = 0x200;
        Workbench wb = new Workbench(ImmutableList.of(Cmp.CPXA.opcode(), Word.lo(addr), Word.hi(addr)));
        wb.registers().x(0x80);
        wb.poke(addr, 0);
        wb.run(1);
        assertTrue(wb.registers().c);
    }

}
