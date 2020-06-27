package com.pjfsw.sixfiveoto.addressables;

import com.pjfsw.sixfiveoto.Word;

public class TestRom implements Peeker {
    private final int startAddress;

    private final int[] bytes;


    public TestRom(int startAddress) {
        this.startAddress = startAddress;
        this.bytes = new int[] {
            0xea,
            0xa9, 0x11,
            0xae, Word.lo(startAddress), Word.hi(startAddress),
            0xbd, Word.lo(startAddress), Word.hi(startAddress),
            0xe8,
            0x4c, Word.lo(startAddress), Word.hi(startAddress)
        };
    }

    @Override
    public int peek(int address) {
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
