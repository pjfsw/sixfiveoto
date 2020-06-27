package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class JmpTest {
    @Test
    public void testAbsolute() {
        Workbench wb = new Workbench(new Jmp.Absolute().assemble(1234));
        wb.run(1);
        assertEquals(1234, wb.registers().pc);
        assertEquals(3, wb.cycles());
    }
}
