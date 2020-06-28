package com.pjfsw.sixfiveoto.registers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;

public class RegistersTest {

    @Test
    public void testPush() {
        Registers registers = new Registers();
        Stack stack = new Stack();
        registers.sp = 0x00;
        registers.sp(stack, 17);
        assertEquals(17, stack.peek(0x100));
        assertEquals(0xFF, registers.sp);
    }

    @Test
    public void testPop() {
        Registers registers = new Registers();
        Stack stack = new Stack();
        registers.sp = 0xFF;
        stack.poke(0x100, 17);
        assertEquals(17,registers.sp(stack));
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
