package com.pjfsw.sixfiveoto.addressables;

import com.pjfsw.sixfiveoto.Word;

public class MemoryModule implements Peeker, Poker {
    private final int[] bytes;
    private final int capacity;

    public static MemoryModule create32K() {
        return new MemoryModule(15);
    }

    public static MemoryModule create8K() {
        return new MemoryModule(13);
    }

    /**
     * Create a RAM block
     * @param capacityBits Capacity in number of address lines (15 = 2^15 = 32K)
     */
    public MemoryModule(int capacityBits) {
        this.capacity = 1 << capacityBits;
        this.bytes = new int[capacity];
    }


    @Override
    public int peek(int address) {
        return bytes[address & (capacity-1)];
    }

    @Override
    public void poke(final int address, final int data) {
        bytes[address & (capacity-1)] = Word.lo(data);
    }

}
