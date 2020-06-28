package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Workbench;

public class PhaPlaTest {
    @Test
    public void test() {
        Workbench wb = new Workbench(ImmutableList.<Integer>builder()
            .addAll(new Pha().assemble(0))
            .addAll(new Pla().assemble(0))
            .build());
        wb.registers().a(17);
        assertEquals(3, wb.run(1)); // PHA
        wb.registers().a(99);
        assertEquals(4, wb.run(1)); // PLA
        assertEquals(17, wb.registers().a());
    }
}
