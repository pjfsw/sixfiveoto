package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Workbench;

public class BneTest {
    @Test
    public void testZero() {
        Workbench wb = new Workbench(ImmutableList.of(Branch.BNE.opcode(), 127));
        wb.registers().a(0);
        assertEquals(2, wb.run(1));
        assertEquals(Workbench.CODEBASE + 2, wb.registers().pc);
    }

    @Test
    public void testNonZero() {
        Workbench wb = new Workbench(ImmutableList.of(Branch.BNE.opcode(), 127));
        wb.registers().a(1);
        assertEquals(3, wb.run(1));
        assertEquals(Workbench.CODEBASE + 129, wb.registers().pc);
    }

    @Test
    public void testPageBoundary() {
        Workbench wb = new Workbench(Collections.emptyList());
        wb.registers().pc = 0x2FD;
        wb.poke(0x2FD, Branch.BNE.opcode());
        wb.poke(0x2FE, 1);
        wb.registers().a(1);
        assertEquals(4, wb.run(1));
        assertEquals(0x300, wb.registers().pc);
    }

}
