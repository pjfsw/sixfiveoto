package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class NopTest {
    @Test
    public void test() {
        Workbench wb = new Workbench(new Nop().assemble(0));
        wb.run(1);
        assertEquals(2, wb.cycles());
    }
}
