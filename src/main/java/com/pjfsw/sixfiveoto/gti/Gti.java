package com.pjfsw.sixfiveoto.gti;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Resettable;
import com.pjfsw.sixfiveoto.addressables.via.Pin;

/**
 * Generic Transfer Interface
 *
 * Clock (OUT) (SPI CLK)
 * Bit in (IN) (SPI MISO)
 * Bit out (OUT) (SPI MOSI)
 * /Slave select (OUT)
 * Slave ready (IN)
 *
 * CPU flow:
 * Clock = 0
 * Slave select = 0
 * Wait Slave Ready = 0
 * Write bit
 * Clock = 1
 * Wait Slave Ready = 1
 * Read bit
 * .. repeat 7 times
 *
 * Repeat for X number of times as defined by application protocol
 *
 * Set Slave Select=1
 *
 * A value of 0 means no data - TODO escaping
 *
 */
public class Gti implements Clockable, Resettable {
    private final int capacity;
    private final Pin out = new Pin();
    private final Pin ready = new Pin();
    private final Pin in = new Pin();
    private final Pin clock = new Pin();
    private final Pin notSelected = new Pin();
    private final Pin connected = new Pin();

    private final Queue<Integer> toWorld;
    private final Queue<Integer> toCpu;
    int toCpuByte = 0;
    int toWorldByte = 0;
    int position = 0 ;
    boolean internalClock = false;

    public Gti(int capacity) {
        this.capacity = capacity;
        toWorld = new ArrayBlockingQueue<>(capacity);
        toCpu = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void next(final int cycles) {
        if (notSelected.value) {
            resetState();
        } else if (position == 8) {
            if (toWorldByte > 0) {
                toWorld.offer(toWorldByte);
                toWorldByte = 0;
            }
            position = 0;
        } else if (clock.value && !internalClock) {
            int bitPosition = 7-position; // MSB first
            toWorldByte |= (in.value ? 1 : 0) << bitPosition; // MSB first
            if (position == 0) {
                toCpuByte = getNextToCpuByte();
            }
            out.value = (toCpuByte & (1 << bitPosition)) != 0;
            ready.value = true;
            position++;
        }
        if (!connected.value) {
            toWorld.clear();
        }
        if (!clock.value) {
            ready.value = toWorld.size() == capacity;
        }
        internalClock = clock.value;
    }

    private int getNextToCpuByte() {
        Integer value = toCpu.poll();
        return (value != null) ? value : 0;
    }

    public int read() {
        Integer value = toWorld.poll();
        return value == null ? -1 : value;
    }

    public boolean write(int data) {
        return toCpu.offer(data);
    }

    @Override
    public void reset() {
        resetState();
        toWorld.clear();
    }

    private void resetState() {
        out.value = false;
        ready.value = false;
        position = 0;
        toCpuByte = 0;
        toWorldByte = 0;
        internalClock = false;
        toCpu.clear();  // Don't care about input when not selected
    }

    public Pin getSlaveOut() {
        return out;
    }

    public Pin getSlaveReady() {
        return ready;
    }

    public Pin getClockIn() {
        return clock;
    }

    public Pin getSlaveIn() {
        return in;
    }

    public Pin getSlaveSelect() {
        return notSelected;
    }

    public Pin getConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected.value = connected;
    }

}
