package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Workbench;

public class LdxTest {
    private static final int POSITIVE = 17;
    private static final int NEGATIVE = 0xF0;
    private static final int ZERO = 0 ;

    @Test
    public void testImmediate() {
        Workbench wb = new Workbench(ImmutableList.of(
            LdImmediate.LDX.opcode(), POSITIVE,
            LdImmediate.LDX.opcode(), NEGATIVE,
            LdImmediate.LDX.opcode(), ZERO));
        assertEquals(2, wb.run(1));
        assertEquals(POSITIVE, wb.registers().x());
        assertFalse(wb.registers().z);
        assertFalse(wb.registers().n);
        assertEquals(2, wb.run(1));
        assertEquals(NEGATIVE, wb.registers().x());
        assertFalse(wb.registers().z);
        assertTrue(wb.registers().n);
        assertEquals(2, wb.run(1));
        assertEquals(ZERO, wb.registers().x());
        assertTrue(wb.registers().z);
        assertFalse(wb.registers().n);
    }
}
