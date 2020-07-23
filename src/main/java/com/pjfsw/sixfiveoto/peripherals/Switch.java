package com.pjfsw.sixfiveoto.peripherals;

import com.pjfsw.sixfiveoto.addressables.Connectable;
import com.pjfsw.sixfiveoto.addressables.via.Pin;

public class Switch implements Connectable {
    private final Pin button = Pin.input();
    private final boolean inverted;

    /**
     * Create a switch
     *
     * @param inverted if set to true then 0 means pressed, 1 released
     */
    private Switch(boolean inverted) {
        this.inverted = inverted;
        this.button.value = inverted;
    }

    public static Switch inverted() {
        return new Switch(true);
    }

    public static Switch normal() {
        return new Switch(false);
    }

    public Pin getPin() {
        return button;
    }

    public void setState(boolean enabled) {
        this.button.value = inverted ^ enabled;

    }

    @Override
    public Pin getPin(final String pinName) {
        return button;
    }
}
