package com.pjfsw.sixfiveoto.spi;

import com.pjfsw.sixfiveoto.addressables.via.Pin;

public class Spi {
    private final Pin slaveOut;
    private final Pin slaveIn;
    private final Pin clock;
    private final Pin slaveNotSelected; // Active low
    private int position;
    private boolean internalClock;
    private int toDeviceByte;
    private int toMasterByte;

    public Spi() {
        slaveOut = new Pin();
        slaveIn = new Pin();
        clock = new Pin();
        slaveNotSelected = new Pin();
        resetState();
    }

    public void update() {
        if (slaveNotSelected.value) {
            resetState();
        } else if (clock.value && !internalClock) {
            int relativePosition = position % 8;
            if (relativePosition == 0) {
                toDeviceByte = 0;
            }
            int bitPosition = 7 - relativePosition; // MSB first
            toDeviceByte |= (slaveIn.value ? 1 : 0) << bitPosition;
            slaveOut.value = (toMasterByte & (1 << bitPosition)) != 0;
            position++;
        }

        internalClock = clock.value;
    }

    public void setFromDeviceData(int data) {
        this.toMasterByte = data;
    }

    public int getToDeviceData() {
        return this.toDeviceByte;
    }

    public int getPosition() {
        return position;
    }

    private void resetState() {
        position = 0;
        toDeviceByte = 0;
        internalClock = false;
    }

    public Pin getSlaveOut() {
        return slaveOut;
    }

    public Pin getSlaveIn() {
        return slaveIn;
    }

    public Pin getClock() {
        return clock;
    }

    public Pin getSlaveSelect() {
        return slaveNotSelected;
    }
}
