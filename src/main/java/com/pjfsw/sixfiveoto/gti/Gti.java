package com.pjfsw.sixfiveoto.gti;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Resettable;

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
 * Example: Simple terminal bi directional transfer
 *
 * Master  Slave
 * XX      YY        Master has XX bytes to send, slave has YY bytes to send*
 * bn      bn        Exchange bytes, either end write 00 when no more to send
 *
 * Alternative: us ascii optimized bi directional transfer
 *
 * Master       Slave
 * nxxx xxxx    nyyy yyyy    n=0 x/y contains the 7-bit value to send
 *                           n=1 x/y bytes to send
 * bbbb bbbb    bbbb bbbb    if n=1 exchange remaining bytes, write 00 when no more to send
 *
 */
public class Gti implements Clockable, Resettable {
    private final int capacity;
    boolean out = false;
    boolean ready = false;
    boolean in = false;
    boolean clock = false;
    boolean selected = false;
    private final Queue<Integer> toWorld;
    private final Queue<Integer> toCpu;
    int toCpuByte = 0;
    int toWorldByte = 0;
    int position = 0 ;
    int bytePosition = 0 ;
    boolean internalClock = false;

    public Gti(int capacity) {
        this.capacity = capacity;
        toWorld = new ArrayBlockingQueue<>(capacity);
        toCpu = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void next(final int cycles) {
        if (!selected) {
            resetState();
        } else if (position == 8) {
            toWorld.offer(toWorldByte);
            position = 0;
            bytePosition++;
        } else if (clock && !internalClock) {
            int bitPosition = 7-position; // MSB first
            toWorldByte |= (in ? 1 : 0) << bitPosition; // MSB first
            if (position == 0) {
                toCpuByte = getNextToCpuByte();
            }
            out = (toCpuByte & (1 << bitPosition)) != 0;
            ready = true;
            position++;
        } else if (!clock) {
            ready = toWorld.size() == capacity;
        }
        internalClock = clock;
    }

    private int getNextToCpuByte() {
        if (bytePosition == 0) {
            return toCpu.size();
        } else {
            Integer value = toCpu.poll();
            return (value != null) ? value : 0;
        }
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
        toCpu.clear();
    }

    private void resetState() {
        out = false;
        ready = false;
        position = 0;
        toCpuByte = 0;
        toWorldByte = 0;
        internalClock = false;
        bytePosition = 0;
    }

    public Supplier<Boolean> getSlaveOut() {
        return () -> out;
    }

    public Supplier<Boolean> getSlaveReady() {
        return () -> ready;
    }

    public Consumer<Boolean> getClockIn() {
        return (clock) -> this.clock = clock;
    }

    public Consumer<Boolean> getSlaveIn() {
        return (data) -> this.in = data;
    }

    public Consumer<Boolean> getSlaveSelect() {
        return (select) -> this.selected = !select; // active low
    }

}
