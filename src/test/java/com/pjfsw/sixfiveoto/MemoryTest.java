package com.pjfsw.sixfiveoto;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class MemoryTest {
    @Test
    public void testAdd() {
        assertEquals(0x0110, Memory.add(0x0100, 0x010));
    }

    @Test
    public void testAddWrap() {
        assertEquals(0x0000, Memory.add(0xFFF0, 0x010));
    }

    @Test
    public void testAddSigned() {
        assertEquals(0x0110, Memory.addSigned(0x0100, 0x010));
    }

    @Test
    public void testAddSignedWrap() {
        assertEquals(0x0000, Memory.addSigned(0xFFF0, 0x010));
    }

    @Test
    public void testAddSignedNegative() {
        assertEquals(0x02F0, Memory.addSigned(0x0300, 0x0F0));
    }

    @Test
    public void testAddSignedNegativeWrap() {
        assertEquals(0xFFF0, Memory.addSigned(0x0000, 0x0F0));
    }

    @Test
    public void testPenaltyNone() {
        assertEquals(0, Memory.penalty(0xF000, 0xF080));
    }

    @Test
    public void testPenaltyForward() {
        assertEquals(1, Memory.penalty(0xF0F0, 0xF100));
    }

    @Test
    public void testPenaltyBackward() {
        assertEquals(1, Memory.penalty(0xF100, 0xF0F0));
    }

}
