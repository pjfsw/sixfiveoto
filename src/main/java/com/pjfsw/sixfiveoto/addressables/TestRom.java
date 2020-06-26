package com.pjfsw.sixfiveoto.addressables;

import java.util.function.Function;

public class TestRom implements Function<Integer, Integer> {
    private final int startAddress;

    private final int[] bytes;

    public TestRom(int startAddress) {
        this.startAddress = startAddress;
        this.bytes = new int[] {
            0xea,
            0x4c, startAddress & 0xFF, startAddress >> 8
        };
    }

    @Override
    public Integer apply(final Integer address) {
        if (address == 0xfffc) {
            return startAddress & 0xFF;
        } else if (address == 0xfffd) {
            return startAddress >> 8;
        } else {
            int ofs = address - startAddress;
            if (ofs >= 0 && ofs <  bytes.length) {
                return bytes[ofs];
            }
        }
        return 0;
    }
}
