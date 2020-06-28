package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Workbench;

public class JsrRtsTest {
    @Test
    public void test() {
        Workbench wb = new Workbench(ImmutableList.<Integer>builder()
            .addAll(new Jsr().assemble(Workbench.CODEBASE+4))
            .addAll(new Inx().assemble(0))
            .addAll(new Ldx.Immediate().assemble(16))
            .addAll(new Rts().assemble(0))
            .build());
        assertEquals(6, wb.run(1)); // JSR
        wb.run(1);                  // LDX
        assertEquals(6, wb.run(1)); // RTS
        wb.run(1);
        assertEquals(17, wb.registers().x());
    }
}
