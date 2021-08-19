package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Workbench;

public class SeiTest {
    @Test
    public void test() {
        Workbench wb = new Workbench(ImmutableList.of(Sei.OPCODE));
        wb.registers().i(false);
        assertEquals(2, wb.run(1));
        assertTrue(wb.registers().i());
    }
}
