package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class ModifyFlagsTest {
    @Test
    public void testClc() {
        Workbench wb = new Workbench(ModifyFlags.CLC.opcode());
        wb.registers().c = true;
        assertEquals(2,wb.run(1));
        assertFalse(wb.registers().c);
    }

    @Test
    public void testSec() {
        Workbench wb = new Workbench(ModifyFlags.SEC.opcode());
        wb.registers().c = false;
        assertEquals(2,wb.run(1));
        assertTrue(wb.registers().c);
    }

    @Test
    public void testClv() {
        Workbench wb = new Workbench(ModifyFlags.CLV.opcode());
        wb.registers().v = true;
        assertEquals(2,wb.run(1));
        assertFalse(wb.registers().v);
    }

}
