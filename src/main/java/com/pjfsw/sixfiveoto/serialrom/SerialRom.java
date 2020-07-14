package com.pjfsw.sixfiveoto.serialrom;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.spi.Spi;

public class SerialRom implements Clockable, Drawable {
    private static final int CAPACITY = 0x20000;
    private final int usedSize;

    private Instruction instruction;
    private int address;

    private final int[] contents;
    private final Spi spi;
    private int lastPosition;

    private enum Instruction {
        READ(0b00000011),
        IDLE(0b11111111);

        private final int code;

        Instruction(int code) {
            this.code = code;
        }

        public static Instruction parse(int code) {
            for (Instruction instruction : values()) {
                if (instruction.code == code) {
                    return instruction;
                }
            }
            return IDLE;
        }

    }

    public SerialRom(Spi spi, int[] contents) {
        this.spi = spi;
        this.contents = new int[CAPACITY];

        Arrays.fill(this.contents, 0xFF);
        int copySize = contents.length;
        if (contents.length > CAPACITY) {
            System.out.println(String.format(
                "- Serial ROM warning: provided data size is %d bytes > %d bytes capacity, clipping.",
                contents.length,
                CAPACITY
            ));
            copySize = CAPACITY;
        }
        System.arraycopy(contents, 0, this.contents, 0, copySize);
        this.usedSize = copySize;
    }

    @Override
    public void next(final int cycles) {
        spi.update();
        if (spi.getSlaveSelect().value || spi.getPosition() < 8 || spi.getPosition() == lastPosition) {
            return;
        }
        if (spi.getPosition() == 8) {
            instruction = Instruction.parse(spi.getToDeviceData());
        } else if (spi.getPosition() % 8 == 0) {
            if (instruction == Instruction.READ) {
                read(spi.getPosition()/8);
            }
        }
        lastPosition = spi.getPosition();
    }

    private void read(int byteNo) {
        if (byteNo == 2) {
            address = (spi.getToDeviceData() & 1) << 16;
        } else if (byteNo == 3) {
            address |= spi.getToDeviceData() << 8;
        } else {
            if (byteNo == 4) {
                address |= spi.getToDeviceData();
            }

            spi.setFromDeviceData(address >= contents.length ? 0xFF : contents[address]);
            address = (address + 1) % CAPACITY;
        }
    }

    @Override
    public void draw(final Graphics graphics) {
        Graphics2D g2 = (Graphics2D)graphics;

        int h = 3;
        g2.setColor(Color.RED);
        g2.fillRect(0,0, 100, h);
        g2.setColor(Color.GREEN);
        g2.fillRect(0,0, usedSize * 100 / CAPACITY, h);
        g2.setColor(Color.WHITE);
        g2.drawLine(address,0, address, h);

    }
}
