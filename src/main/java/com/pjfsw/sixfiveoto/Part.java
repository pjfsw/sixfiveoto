package com.pjfsw.sixfiveoto;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Connectable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.addressables.Resettable;
import com.pjfsw.sixfiveoto.peripherals.Switch;
import com.pjfsw.sixfiveoto.spi.Spi;

public interface Part {
    Clockable getClockable();

    Resettable getResettable();

    Drawable getDrawable();

    Peeker getPeeker();

    Poker getPoker();

    Spi getSpi();

    PartType getType();

    Switch getSwitch();

    Connectable getConnectable();

    static Part createSwitch(Switch aSwitch) {
        return new Part() {
            @Override
            public Clockable getClockable() {
                return null;
            }

            @Override
            public Resettable getResettable() {
                return null;
            }

            @Override
            public Drawable getDrawable() {
                return null;
            }

            @Override
            public Peeker getPeeker() {
                return null;
            }

            @Override
            public Poker getPoker() {
                return null;
            }

            @Override
            public Spi getSpi() {
                return null;
            }

            @Override
            public PartType getType() {
                return PartType.SWITCH;
            }

            @Override
            public Switch getSwitch() {
                return aSwitch;
            }

            @Override
            public Connectable getConnectable() {
                return aSwitch;
            }

        };
    }

    static Part create(
        PartType type,
        Peeker peeker,
        Poker poker,
        Clockable clockable,
        Resettable resettable,
        Drawable drawable,
        Connectable connectable
    ) {
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
            public Poker getPoker() {
                return poker;
            }

            @Override
            public Spi getSpi() {
                return null;
            }

            @Override
            public PartType getType() {
                return type;
            }

            @Override
            public Switch getSwitch() {
                return null;
            }

            @Override
            public Connectable getConnectable() {
                return connectable;
            }
        };
    }

    static Part spiPart(Spi spi) {
        return new Part() {
            @Override
            public Clockable getClockable() {
                return null;
            }

            @Override
            public Resettable getResettable() {
                return null;
            }

            @Override
            public Drawable getDrawable() {
                return null;
            }

            @Override
            public Peeker getPeeker() {
                return null;
            }

            @Override
            public Poker getPoker() {
                return null;
            }

            @Override
            public Spi getSpi() {
                return spi;
            }

            @Override
            public PartType getType() {
                return PartType.SPI;
            }

            @Override
            public Switch getSwitch() {
                return null;
            }

            @Override
            public Connectable getConnectable() {
                return spi;
            }
        };
    }
}
