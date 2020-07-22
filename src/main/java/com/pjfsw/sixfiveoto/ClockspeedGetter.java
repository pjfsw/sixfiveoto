package com.pjfsw.sixfiveoto;

public class ClockspeedGetter {
    static final String CLOCKSPEED_PROPERTY = "clockspeed";

    static int getClockSpeed(Config properties) {
        return Integer.parseInt(properties.getProperty(CLOCKSPEED_PROPERTY, "1000000"));
    }
}
