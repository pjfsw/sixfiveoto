package com.pjfsw.sixfiveoto.instruction;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.TestBench;

public class InxTest {
    @Test
    public void testNormal() {
        TestBench tb = new TestBench(new Inx().assemble(0));
        tb.run(1);
        assertEquals(1,tb.registers().x);
        assertEquals(2, tb.cycles());
    }

    @Test
    public void testWrap() {
        TestBench tb = new TestBench(new Inx().assemble(0));
        tb.registers().x = 255;
        tb.run(1);
        assertEquals(0,tb.registers().x);
        assertEquals(2, tb.cycles());
    }
}