package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;
import com.pjfsw.sixfiveoto.instruction.Jmp.Absolute;

public class JmpTest {
    @Test
    public void testAbsolute() {
        Workbench wb = new Workbench(ImmutableList.of(Absolute.OPCODE, Word.lo(1234), Word.hi(1234)));
        wb.run(1);
        assertEquals(1234, wb.registers().pc);
        assertEquals(3, wb.cycles());
    }
}
