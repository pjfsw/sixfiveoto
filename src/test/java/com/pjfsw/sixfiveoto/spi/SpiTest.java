package com.pjfsw.sixfiveoto.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        assertNull(spi.getSlaveOut().value);
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
            actualFromDevice |= (spi.getSlaveOut().value ? 1 : 0) << bitPos;
            spi.getSlaveIn().value = (expectedToDevice & (1<<bitPos)) != 0;
            spi.getClock().value = true;
            spi.update();
            spi.getClock().value = false;
            spi.update();
        }
        assertEquals(0x5F, actualFromDevice);
        assertEquals(expectedToDevice, spi.getToDeviceData());
    }

    @Test
    public void derpesTest() {
        for (int i = 0; i < 36; i++) {
            double f = 440 * Math.pow(2, ((double)i/(double)12));
            System.out.println((int)f + ", ");
        }
    }

}
