package com.pjfsw.sixfiveoto.addressables;

import com.pjfsw.sixfiveoto.Word;

public class RomVectors implements Peeker {
    private final int bootVector;
    private final int irqVector;

    public RomVectors(int bootVector, int irqVector) {
        this.bootVector = bootVector;
        this.irqVector = irqVector;
    }

    @Override
    public int peek(final int address) {
        if (Word.lo(address) == 0xFC) {
            return Word.lo(bootVector);
        } else if (Word.lo(address) == 0xFD) {
            return Word.hi(bootVector);
        } else if (Word.lo(address) == 0xFE) {
            return Word.lo(irqVector);
        } else if (Word.lo(address) == 0xFF) {
            return Word.hi(irqVector);
        } else {
            return 0;
        }
   }
}
