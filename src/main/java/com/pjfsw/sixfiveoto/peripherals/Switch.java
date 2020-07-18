package com.pjfsw.sixfiveoto.peripherals;

import com.pjfsw.sixfiveoto.addressables.via.Pin;

public class Switch {
    private final Pin button = Pin.input();

    public Pin getPin() {
        return button;
    }

    public void setState(boolean enabled) {
        this.button.value = enabled;

    }
}
