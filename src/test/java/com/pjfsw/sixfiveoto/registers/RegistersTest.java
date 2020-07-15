package com.pjfsw.sixfiveoto.registers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;

public class RegistersTest {
    @Test
    public void testAdcNoCarry() {
        Registers registers = new Registers();
        registers.c = false;
        assertEquals(0x70, registers.adc(0x10, 0x60));
        assertFalse(registers.c);
    }

    @Test
    public void testAdcCarryIn() {
        Registers registers = new Registers();
        registers.c = true;
        assertEquals(0x71, registers.adc(0x10, 0x60));
        assertFalse(registers.c);
    }

    @Test
    public void testAdcCarryOut() {
        Registers registers = new Registers();
        registers.c = false;
        assertEquals(0x00, registers.adc(0x10, 0xF0));
        assertTrue(registers.c);
    }

    @Test
    public void testAdcCarryInOut() {
        Registers registers = new Registers();
        registers.c = true;
        assertEquals(0x01, registers.adc(0x10, 0xF0));
        assertTrue(registers.c);
    }

    @Test
    public void testSbcCarry() {
        Registers registers = new Registers();
        registers.c = true;
        assertEquals(0x70, registers.sbc(0x80, 0x10));
        assertTrue(registers.c);
    }

    @Test
    public void testSbcNegative() {
        Registers registers = new Registers();
        registers.c = true;
        assertEquals(0x10, registers.sbc(0x80, 0x70));
        assertEquals(0xF0, registers.sbc(0x10, 0x20));
        assertFalse(registers.c);
    }


    @Test
    public void testSbcWraparound() {
        Registers registers = new Registers();
        registers.c = true;
        assertEquals(0x02, registers.sbc(0x01, 0xFF));
        assertFalse(registers.v);
    }

    private static boolean overflowAdc(Registers registers, int a, int b) {
        registers.c = false;
        registers.adc(a, b);
        return registers.v;
    }

    private static boolean overflowSbc(Registers registers, int a, int b) {
        registers.c = true;
        registers.sbc(a,b);
        return registers.v;
    }


    @Test
    public void testOverflowAdc() {
        Registers registers = new Registers();
        assertFalse(overflowAdc(registers, 0x50, 0x10)); // 0x60
        assertTrue( overflowAdc(registers, 0x50, 0x50)); // 0xa0
        assertFalse(overflowAdc(registers, 0x50, 0x90)); // 0xe0
        assertFalse(overflowAdc(registers, 0x50, 0xD0)); // 0x120
        assertFalse(overflowAdc(registers, 0xD0, 0x10)); // 0xe0
        assertFalse(overflowAdc(registers, 0xD0, 0x50)); // 0x120
        assertTrue( overflowAdc(registers, 0xD0, 0x90)); // 0x160
        assertFalse(overflowAdc(registers, 0xD0, 0xD0)); // 0x1A0
    }

    @Test
    public void testOverflowSbc() {
        Registers registers = new Registers();
        assertFalse(overflowSbc(registers, 0x50, 0xf0)); // 0x60
        assertTrue( overflowSbc(registers, 0x50, 0xb0)); // 0xa0
        assertFalse(overflowSbc(registers, 0x50, 0x70)); // 0xe0
        assertFalse(overflowSbc(registers, 0x50, 0x30)); // 0x120
        assertFalse(overflowSbc(registers, 0xD0, 0xf0)); // 0xe0
        assertFalse(overflowSbc(registers, 0xD0, 0xB0)); // 0x120
        assertTrue( overflowSbc(registers, 0xD0, 0x70)); // 0x160
        assertFalse(overflowSbc(registers, 0xD0, 0x30)); // 0x1A0
    }

    @Test
    public void testPush() {
        Registers registers = new Registers();
        Stack stack = new Stack();
        registers.sp = 0x00;
        registers.push(stack, 17);
        assertEquals(17, stack.peek(0x100));
        assertEquals(0xFF, registers.sp);
    }

    @Test
    public void testPop() {
        Registers registers = new Registers();
        Stack stack = new Stack();
        registers.sp = 0xFF;
        stack.poke(0x100, 17);
        assertEquals(17,registers.pull(stack));
        assertEquals(0, registers.sp);
    }

    @Test
    public void testAsl() {
        Registers registers = new Registers();

        assertEquals(0x60, registers.asl(0x30));
        assertFalse(registers.c);
        assertFalse(registers.z);
        assertFalse(registers.n);
        assertEquals(0xc0, registers.asl(0x60));
        assertFalse(registers.c);
        assertFalse(registers.z);
        assertTrue(registers.n);
        assertEquals(0x80, registers.asl(0xc0));
        assertTrue(registers.c);
        assertFalse(registers.z);
        assertTrue(registers.n);
        assertEquals(0x00, registers.asl(0x80));
        assertTrue(registers.c);
        assertTrue(registers.z);
        assertFalse(registers.n);
        assertEquals(0x00, registers.asl(0x00));
        assertFalse(registers.c);
        assertTrue(registers.z);
        assertFalse(registers.n);
    }

    @Test
    public void testLsr() {
        Registers registers = new Registers();

        assertEquals(0x01, registers.lsr(0x02));
        assertFalse(registers.c);
        assertFalse(registers.z);
        assertFalse(registers.n);

        assertEquals(0x00, registers.lsr(0x01));
        assertTrue(registers.c);
        assertTrue(registers.z);
        assertFalse(registers.n);

        assertEquals(0x00, registers.lsr(0x00));
        assertFalse(registers.c);
        assertTrue(registers.z);
        assertFalse(registers.n);
    }

    @Test
    public void testRol() {
        Registers registers = new Registers();
        registers.c = false;
        assertEquals(0x40, registers.rol(0x20));
        assertFalse(registers.c);
        assertFalse(registers.z);
        assertFalse(registers.n);
        assertEquals(0x80, registers.rol(0x40));
        assertFalse(registers.c);
        assertFalse(registers.z);
        assertTrue(registers.n);
        assertEquals(0x00, registers.rol(0x80));
        assertTrue(registers.c);
        assertTrue(registers.z);
        assertFalse(registers.n);
        assertEquals(0x01, registers.rol(0x00));
        assertFalse(registers.c);
        assertFalse(registers.z);
        assertFalse(registers.n);
    }

    @Test
    public void testRor() {
        Registers registers = new Registers();
        registers.c = false;
        assertEquals(0x1, registers.ror(0x02));
        assertFalse(registers.c);
        assertFalse(registers.z);
        assertFalse(registers.n);
        assertEquals(0x0, registers.ror(0x01));
        assertTrue(registers.c);
        assertTrue(registers.z);
        assertFalse(registers.n);
        assertEquals(0x80, registers.ror(0x0));
        assertFalse(registers.c);
        assertFalse(registers.z);
        assertTrue(registers.n);
    }

    private static class Stack implements Peeker, Poker {
        private final int[] bytes = new int[512];

        @Override
        public int peek(final int address) {
            return bytes[address];
        }

        @Override
        public void poke(final int address, final int data) {
            bytes[address] = data;
        }
    }
}
