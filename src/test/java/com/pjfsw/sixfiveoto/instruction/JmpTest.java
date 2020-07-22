package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Word;
import com.pjfsw.sixfiveoto.Workbench;

public class JmpTest {
    @Test
    public void testAbsolute() {
        Workbench wb = new Workbench(Jmp.OPCODE, Word.lo(1234), Word.hi(1234));
        wb.run(1);
        assertEquals(1234, wb.registers().pc);
        assertEquals(3, wb.cycles());
    }

    @Test
    public void testIndirect() {
        int pointer = 0x200;
        int address = 0x300;
        Workbench wb = new Workbench(JmpIndirect.OPCODE, Word.lo(pointer), Word.hi(pointer));
        wb.poke(pointer, Word.lo(address));
        wb.poke(pointer+1, Word.hi(address));
        wb.run(1);
        assertEquals(address, wb.registers().pc);
        assertEquals(6, wb.cycles());
    }

    @Test
    public void testIndexedIndirect() {
        int pointer = 0x200;
        int offset = 0x03;
        int address = 0x300;
        Workbench wb = new Workbench(JmpIndexedIndirect.OPCODE, Word.lo(pointer), Word.hi(pointer));
        wb.poke(pointer+offset, Word.lo(address));
        wb.poke(pointer+offset+1, Word.hi(address));
        wb.registers().x(offset);
        wb.run(1);
        assertEquals(address, wb.registers().pc);
        assertEquals(6, wb.cycles());
    }

}
