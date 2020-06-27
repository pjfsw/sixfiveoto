package com.pjfsw.sixfiveoto.addressables;

import com.pjfsw.sixfiveoto.Word;

public class RomVectors implements Peeker {
    private final int bootVector;

    public RomVectors(int bootVector) {
        this.bootVector = bootVector;
    }

    @Override
    public int peek(final int address) {
        if (Word.lo(address) == 0xFC) {
            return Word.lo(bootVector);
        } else if (Word.lo(address) == 0xFD) {
            return Word.hi(bootVector);
        } else {
            return 0;
        }
    }
}
