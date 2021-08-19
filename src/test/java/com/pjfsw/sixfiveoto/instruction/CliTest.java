package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class CliTest {
    @Test
    public void test() {
        Workbench wb = new Workbench(Cli.OPCODE);
        wb.registers().i(true);
        assertEquals(2, wb.run(1));
        assertFalse(wb.registers().i());
    }
}
