package com.pjfsw.sixfiveoto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.pjfsw.sixfiveoto.instruction.Lda;

public class CpuTest {
    @Test
    public void testBasic() {
        ImmutableList<Integer> byteCode = ImmutableList.<Integer>builder()
            .addAll(new Lda.Immediate().assemble(37))
            .build();
        TestBench tb = new TestBench(byteCode);
        tb.run(1);
        assertEquals(37, tb.registers().a);
        assertEquals(2, tb.cpu().getCycles());
    }
}
