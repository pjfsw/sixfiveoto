package com.pjfsw.sixfiveoto.gti;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 *  *
 *  * CPU flow:
 *  * Clock = 0
 *  * Slave select = 0
 *  * Wait Slave Ready = 0
 *  * Write bit
 *  * Clock = 1
 *  * Wait Slave Ready = 1
 *  * Read bit
 *  * .. repeat 7 times
 *  *
 *  * Repeat for X number of times as defined by application protocol
 *  *
 *  * Set Slave Select=1
 */
public class GtiTest {
    private int exchangeBytes(Gti gti, int fromCpu) {
        int actual = 0;
        gti.getClockIn().accept(false);
        gti.getSlaveSelect().accept(false);
        for (int i = 0; i < 8; i++) {
            gti.next(1);
            int bitPosition = 7-i;
            assertFalse(gti.getSlaveReady().get());
            gti.getSlaveIn().accept((fromCpu & (1 << bitPosition)) != 0);
            gti.getClockIn().accept(true);
            gti.next(1);
            assertTrue(gti.getSlaveReady().get());
            actual |= (gti.getSlaveOut().get() ? 1 : 0) << bitPosition;
            gti.getClockIn().accept(false);
        }
        gti.next(1);
        return actual;
    }

    @Test
    public void test() {
        int toCpuExpected = 0x5e;
        int fromCpuExpected = 0xa5;
        Gti gti = new Gti(2);
        gti.setConnected(true);
        assertTrue(gti.write(toCpuExpected));
        assertEquals(-1, gti.read());
        assertEquals(1, exchangeBytes(gti, fromCpuExpected));
        assertEquals(toCpuExpected, exchangeBytes(gti, fromCpuExpected));
        assertEquals(fromCpuExpected, gti.read());
    }

    @Test
    public void testQueueFull() {
        int toCpuExpected = 0x5e;
        int fromCpuExpected = 0xa5;
        Gti gti = new Gti(2);
        gti.setConnected(true);
        assertTrue(gti.write(toCpuExpected));
        assertEquals(-1, gti.read());
        assertEquals(1, exchangeBytes(gti, fromCpuExpected));
        assertEquals(toCpuExpected, exchangeBytes(gti, fromCpuExpected));
        assertTrue(gti.getSlaveReady().get());
        assertEquals(fromCpuExpected, gti.read());
        gti.next(1);
        assertFalse(gti.getSlaveReady().get());
    }

    @Test
    public void testNotConnected() {
        Gti gti = new Gti(1);
        assertEquals(-1, gti.read());
        exchangeBytes(gti, 17);
        assertFalse(gti.getSlaveReady().get());
        exchangeBytes(gti, 17);
        assertFalse(gti.getSlaveReady().get());
    }
}
