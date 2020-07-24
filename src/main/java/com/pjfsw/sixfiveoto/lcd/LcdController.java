package com.pjfsw.sixfiveoto.lcd;

import com.pjfsw.sixfiveoto.addressables.via.Pin;

/**
 * Emulation of ONE HD44780 controller
 */
public class LcdController {
    private final int[] ddram = new int[128];
    private final Pin rs;
    private final Pin e;
    private final Pin rw;
    private final Pin[] data;
    private int nextByte = 0;
    private boolean oldE = false;

    public LcdController(Pin e, Pin rs, Pin rw, Pin[] data) {
        this.e = e;
        this.rs = rs;
        this.rw = rw;
        this.data = data;
    }

    public int getCharAt(int offset) {
        return ddram[offset & 127];
    }

    public void update(int cycles) {
        if (e.value && !oldE) {
            if (rs.value) {
                int b = 0;
                for (int i = 0; i < 8; i++) {
                    b |= (data[i].value ? 1 : 0) << i;
                }
                ddram[nextByte] = b;
                nextByte = (nextByte + 1) % 128;
                if (nextByte == 40) {
                    nextByte = 64;
                } else if (nextByte == 104) {
                    nextByte = 0;
                }
            }
        }
        oldE = e.value;
    }
}
