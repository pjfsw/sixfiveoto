package com.pjfsw.sixfiveoto;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Connectable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Interrupt;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.addressables.Resettable;
import com.pjfsw.sixfiveoto.keyboard.Keyboard;
import com.pjfsw.sixfiveoto.peripherals.Switch;
import com.pjfsw.sixfiveoto.spi.Spi;

public interface Part {
    Clockable getClockable();

    Resettable getResettable();

    Drawable getDrawable();

    Peeker getPeeker();

    Interrupt getInterrupt();

    Keyboard getKeyboard();

    Poker getPoker();

    Spi getSpi();

    PartType getType();

    Switch getSwitch();

    Connectable getConnectable();

    static Part createSwitch(Switch aSwitch) {
        return Part.builder(PartType.SWITCH)
            .withSwitch(aSwitch)
            .withConnectable(aSwitch)
            .build();
    }

    static Builder builder(PartType type) {
        return new Builder(type);
    }

    static Part spiPart(Spi spi) {
        return Part.builder(PartType.SPI)
            .withSpi(spi)
            .withConnectable(spi)
            .build();
    }

    class Builder {
        private final PartType type;
        private Peeker peeker;
        private Poker poker;
        private Clockable clockable;
        private Resettable resettable;
        private Drawable drawable;
        private Connectable connectable;
        private Interrupt interrupt;
        private Spi spi;
        private Switch aSwitch;
        private Keyboard keyboard;

        Builder(PartType type) {
            this.type = type;
        }

        Builder withPeeker(Peeker peeker) {
            this.peeker = peeker;
            return this;
        }

        Builder withPoker(Poker poker) {
            this.poker = poker;
            return this;
        }

        Builder withClockable(Clockable clockable) {
            this.clockable = clockable;
            return this;
        }

        Builder withResettable(Resettable resettable) {
            this.resettable = resettable;
            return this;
        }

        Builder withDrawable(Drawable drawable) {
            this.drawable = drawable;
            return this;
        }

        Builder withConnectable(Connectable connectable) {
            this.connectable = connectable;
            return this;
        }

        Builder withInterrupt(Interrupt interrupt) {
            this.interrupt = interrupt;
            return this;
        }

        Builder withSpi(Spi spi) {
            this.spi = spi;
            return this;
        }

        Builder withSwitch(Switch aSwitch) {
            this.aSwitch = aSwitch;
            return this;
        }

        Builder withKeyboard(Keyboard keyboard) {
            this.keyboard = keyboard;
            return this;
        }


        Part build() {
            return new Part() {
                @Override
                public Clockable getClockable() {
                    return clockable;
                }

                @Override
                public Resettable getResettable() {
                    return resettable;
                }

                @Override
                public Drawable getDrawable() {
                    return drawable;
                }

                @Override
                public Peeker getPeeker() {
                    return peeker;
                }

                @Override
                public Interrupt getInterrupt() {
                    return interrupt;
                }

                @Override
                public Keyboard getKeyboard() {
                    return keyboard;
                }

                @Override
                public Poker getPoker() {
                    return poker;
                }

                @Override
                public Spi getSpi() {
                    return spi;
                }

                @Override
                public PartType getType() {
                    return type;
                }

                @Override
                public Switch getSwitch() {
                    return aSwitch;
                }

                @Override
                public Connectable getConnectable() {
                    return connectable;
                }
            };
        }

    }
}
