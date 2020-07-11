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
        gti.getClockIn().value = false;
        gti.getSlaveSelect().value = false;
        for (int i = 0; i < 8; i++) {
            gti.next(1);
            int bitPosition = 7-i;
            assertFalse(gti.getSlaveReady().value);
            gti.getSlaveIn().value =((fromCpu & (1 << bitPosition)) != 0);
            gti.getClockIn().value = true;
            gti.next(1);
            assertTrue(gti.getSlaveReady().value);
            actual |= (gti.getSlaveOut().value ? 1 : 0) << bitPosition;
            gti.getClockIn().value = false;
        }
        gti.next(1);
        return actual;
    }

    @Test
    public void test() {
        int toCpuExpected = 0x5e;
        int fromCpuExpected = 0xa5;
        Gti gti = new Gti(1);
        gti.setConnected(true);
        assertTrue(gti.write(toCpuExpected));
        assertEquals(-1, gti.read());
        assertEquals(toCpuExpected, exchangeBytes(gti, fromCpuExpected));
        assertEquals(fromCpuExpected, gti.read());
    }

    @Test
    public void testQueueFull() {
        int toCpuExpected = 0x5e;
        int fromCpuExpected = 0xa5;
        Gti gti = new Gti(1);
        gti.setConnected(true);
        assertTrue(gti.write(toCpuExpected));
        assertEquals(-1, gti.read());
        assertEquals(toCpuExpected, exchangeBytes(gti, fromCpuExpected));
        assertTrue(gti.getSlaveReady().value);
        assertEquals(fromCpuExpected, gti.read());
        gti.next(1);
        assertFalse(gti.getSlaveReady().value);
    }

    @Test
    public void testNotConnected() {
        Gti gti = new Gti(1);
        assertEquals(-1, gti.read());
        exchangeBytes(gti, 17);
        assertFalse(gti.getSlaveReady().value);
        exchangeBytes(gti, 17);
        assertFalse(gti.getSlaveReady().value);
    }
}
