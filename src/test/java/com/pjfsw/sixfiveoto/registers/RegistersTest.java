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
