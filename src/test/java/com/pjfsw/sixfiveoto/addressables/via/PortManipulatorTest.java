package com.pjfsw.sixfiveoto.addressables.via;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class PortManipulatorTest {
    @Test
    public void test() {
        int existingValue = 0b1010_1010;
        int outputValue =   0b1110_1111;
        int ddr         =   0b1111_0000;
        int expected    =   0b1110_1010;

        assertEquals(expected, PortManipulator.combineDdrOutIn(ddr, outputValue, existingValue));
    }

}
