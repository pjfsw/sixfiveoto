package com.pjfsw.sixfiveoto.peripherals;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Switch implements Supplier<Boolean>, Consumer<Boolean> {
    private Boolean enabled = false;

    @Override
    public Boolean get() {
        return enabled;
    }

    @Override
    public void accept(final Boolean enabled) {
        this.enabled = enabled;

    }
}
