package com.pjfsw.sixfiveoto.keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.spi.Spi;
import static java.awt.event.KeyEvent.*;

public class Keyboard implements Clockable {
    private final Spi spi;
    private List<Integer> keyCodes = new ArrayList<>();
    private int keyCode;
    private int lastPosition;
    private boolean shift;
    private boolean alt;

    private static final Map<Integer, Integer> keymap = new HashMap<>();

    public Keyboard(Spi spi) {
        this.spi = spi;
        for (int i = 1; i <= 26; i++) {
            keymap.put(i+64, i);
        }
        keymap.put(VK_SPACE, 0x20);
        keymap.put(VK_PERIOD, 0x2e);
        keymap.put(VK_BACK_SLASH, 0x27); // SE = '''
        keymap.put(VK_SLASH, 0x2d); // SE = '-'
        keymap.put(VK_COMMA, 0x2c);
        keymap.put(VK_MINUS, 0x2b); // SE = '+'
        for (int i = 0; i < 10; i++) {
            keymap.put(VK_0 + i, 0x30 + i);
        }
        keymap.put(VK_BACK_QUOTE, 0x3c);
        keymap.put(VK_ENTER, 0x1b);
        keymap.put(VK_BACK_SPACE, 0x1c);
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
        if (keyCode == VK_SHIFT) {
            shift = pressed;
        } else if (keyCode == VK_ALT) {
            alt = pressed;
        } else if (pressed && keyCodes.size() < 2) {
            int value = keymap.getOrDefault(keyCode, 0);
            //System.out.printf("%02x%n", keyCode);
            if (shift || alt || value != 0) {
                keyCodes.add(value | (shift ? (1 << 7) : 0) | (alt ? (1 << 6) : 0));
            }

            return true;
        }
        return false;
    }
}
