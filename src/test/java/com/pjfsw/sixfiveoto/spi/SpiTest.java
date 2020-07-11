package com.pjfsw.sixfiveoto.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

public class SpiTest {
    @Test
    public void testInactive() {
        Spi spi = new Spi();
        spi.setFromDeviceData(0xFF);
        spi.getSlaveIn().value = true;
        spi.getSlaveSelect().value = true;
        spi.getClock().value = true;
        spi.update();
        spi.getClock().value = false;
        spi.update();
        assertFalse(spi.getSlaveOut().value);
        assertEquals(0, spi.getPosition());
    }

    @Test
    public void testExchange() {
        Spi spi = new Spi();
        spi.setFromDeviceData(0x5F);
        spi.getSlaveSelect().value = false;
        spi.getSlaveIn().value = true;
        spi.update();
        int expectedToDevice = 0xAE;
        int actualFromDevice = 0;

        for (int i = 0; i < 8; i++) {
            int bitPos = 7-i;
            spi.getSlaveIn().value = (expectedToDevice & (1<<bitPos)) != 0;
            spi.getClock().value = true;
            spi.update();
            spi.getClock().value = false;
            spi.update();
            actualFromDevice |= (spi.getSlaveOut().value ? 1 : 0) << bitPos;
        }
        assertEquals(0x5F, actualFromDevice);
        assertEquals(expectedToDevice, spi.getToDeviceData());
    }

}
