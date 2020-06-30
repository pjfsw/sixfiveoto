package com.pjfsw.sixfiveoto.instruction;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class TaxTest {
    @Test
    public void testTax() {
        Workbench wb = new Workbench(singletonList(Transfer.TAX.opcode()));
        wb.registers().a(17);
        assertEquals(2, wb.run(1));
        assertEquals(17, wb.registers().x());

    }
}
