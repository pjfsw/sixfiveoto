package com.pjfsw.sixfiveoto.instruction;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class NopTest {
    @Test
    public void test() {
        Workbench wb = new Workbench(singletonList(Nop.OPCODE));
        wb.run(1);
        assertEquals(2, wb.cycles());
    }
}
