package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.Workbench;

public class IrqTest {
    @Test
    public void test() {
        Workbench wb = Workbench.builder()
            .withCode(Nop.OPCODE, Transfer.TAX.opcode(), IncDecRegister.INX.opcode())
            .withIrq(LdImmediate.LDA.opcode(), 41, Rti.OPCODE)
            .build();
        wb.run(1);
        assertEquals(7, wb.cpu().irq());
        assertEquals(12, wb.run(4));
        assertEquals(42, wb.registers().x());
    }
}
