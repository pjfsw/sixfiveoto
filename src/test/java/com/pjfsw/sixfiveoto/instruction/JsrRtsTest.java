package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class JsrRtsTest {
    @Test
    public void test() {
        Workbench wb = new Workbench(
            ImmutableList.of(
                Jsr.OPCODE, Word.lo(Workbench.CODEBASE+4), Word.hi(Workbench.CODEBASE+4),
                IncDecXY.INX.opcode(),
                LdImmediate.LDX.opcode(), 16,
                Rts.OPCODE
            ));
        assertEquals(6, wb.run(1)); // JSR
        wb.run(1);                  // LDX
        assertEquals(6, wb.run(1)); // RTS
        wb.run(1);
        assertEquals(17, wb.registers().x());
    }
}
