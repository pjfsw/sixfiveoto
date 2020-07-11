package com.pjfsw.sixfiveoto.gameduino;

import java.awt.Graphics;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.spi.Spi;

public class Gameduino implements Drawable, Clockable {
    private final Spi spi;
    private boolean readMode;
    private int address;
    private final int[] registers = new int[32768];
    private int lastPosition;

    public Gameduino(Spi spi) {
        this.spi = spi;
        registers[0x2800] = 0x6D;
    }

    @Override
    public void next(final int cycles) {
        spi.update();
        if (spi.getSlaveSelect().value || spi.getPosition() < 8 || spi.getPosition() == lastPosition) {
            return;
        }
        if (spi.getPosition() == 8) {
            address = (spi.getToDeviceData() & 0x7F) << 8;
            readMode = (spi.getToDeviceData() & 0x80) == 0;
        } else if (spi.getPosition() == 16) {
            address |= spi.getToDeviceData();
            if (readMode) {
                spi.setFromDeviceData(registers[address]);
            }
            address++;
        } else if (spi.getPosition() % 8 == 0) {
            if (readMode) {
                spi.setFromDeviceData(registers[address]);
            } else {
                registers[address] = spi.getToDeviceData();
            }
            address++;
        }
        lastPosition = spi.getPosition();
    }

    @Override
    public void draw(final Graphics graphics) {

    }
}
