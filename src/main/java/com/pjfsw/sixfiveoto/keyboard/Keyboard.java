package com.pjfsw.sixfiveoto.keyboard;

import java.util.ArrayList;
import java.util.List;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.spi.Spi;

public class Keyboard implements Clockable {
    private final Spi spi;
    private List<Integer> keyCodes = new ArrayList<>();
    private int keyCode;
    private int lastPosition;

    public Keyboard(Spi spi) {
        this.spi = spi;
    }

    @Override
    public void next(int cycles) {
        spi.update();
        if (spi.getSlaveSelect().value || spi.getPosition() > 8) {
            return;
        }

        if (spi.getPosition() == 0 && spi.getPosition() != lastPosition) {
            //keyCode = 0x43;
            if (keyCodes.size() > 0) {
                keyCode = keyCodes.remove(0);
            } else {
                keyCode = 0;
            }
            spi.setFromDeviceData(keyCode);
        }
        lastPosition = spi.getPosition();
    }

    public boolean consumeKeyCode(int keyCode, boolean pressed) {
        if (pressed && keyCodes.size() < 10) {
            //System.out.printf("Incoming key %2x%n", keyCode);
            keyCodes.add(keyCode);
            return true;
        }
        return false;
    }
}
