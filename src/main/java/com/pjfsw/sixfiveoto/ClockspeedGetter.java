package com.pjfsw.sixfiveoto;

import java.util.Properties;

public class ClockspeedGetter {
    static final String CLOCKSPEED_PROPERTY = "clockspeed";

    static int getClockSpeed(Properties properties) {
        return Integer.parseInt(properties.getProperty(CLOCKSPEED_PROPERTY, "1000000"));
    }
}
