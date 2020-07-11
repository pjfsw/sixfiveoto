package com.pjfsw.sixfiveoto.addressables.via;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.addressables.Resettable;

public class Via6522 implements Peeker, Poker, Drawable, Resettable, Clockable {
    public static final int W = 320;
    private final BufferedImage img;
    private static final int COLOR_FULL = 0xEE000000;
    private static final int COLOR_DIM  = 0x44000000;
    private static final int COLOR_YELLOW = 0x00FFFF00;
    private static final int COLOR_GREEN = 0x0000FF00;
    private static final int COLOR_RED = 0x00FF0000;

    private static final int COLOR_DDRIN = COLOR_DIM | COLOR_YELLOW;
    private static final int COLOR_DDROUT = COLOR_FULL | COLOR_YELLOW;
    private static final int COLOR_READ0 = COLOR_DIM | COLOR_GREEN;
    private static final int COLOR_READ1 = COLOR_FULL | COLOR_GREEN;
    private static final int COLOR_WRITE0 = COLOR_DIM | COLOR_RED;
    private static final int COLOR_WRITE1 = COLOR_FULL | COLOR_RED;
    private static final int PORTB = 0x00;
    private static final int PORTA = 0x01;
    private static final int DDRB = 0x02;
    private static final int DDRA = 0x03;
    private int porta;
    private int portb;
    private final int[] registers = new int[16];
    private final Pin[] pins = new Pin[16];


    public Via6522() {
        img = new BufferedImage(36,6, TYPE_INT_ARGB);
        for (int i = 0; i < 16; i++) {
            pins[i] = new Pin();
        }
    }

    public void setPin(int port, int pinNumber, Pin pin) {
        pins[(port & 1) * 8 + (pinNumber % 8)] = pin;
    }

    public void reset() {
        registers[DDRA] = 0;
        registers[DDRB] = 0;
        registers[PORTA] = 0;
        registers[PORTB] = 0;
        porta = 0;
        portb = 0;
    }

    @Override
    public int peek(final int address) {
        // For Port A, reading an output pin returns whatever level the output pin is at. Depending
        // For Port B, reading an output pin returns whatever ORB is stored (i.e. from poke)
        int reg = address & 0x0f;
        if (reg == PORTA) {
            return porta;
        } else if (reg == PORTB) {
            return PortManipulator.combineDdrOutIn(registers[DDRB], registers[PORTB], portb);
        } else {
            return registers[reg];
        }
    }

    @Override
    public void poke(final int address, final int data) {
        registers[address & 0x0f] = data;
    }

    private static int getPortColor(int ddr, int port, int bit) {
        if ((ddr & bit) == 0) {
            return ((port & bit) == 0) ? COLOR_READ0 : COLOR_READ1;
        } else {
            return ((port & bit) == 0) ? COLOR_WRITE0 : COLOR_WRITE1;
        }
    }

    public int processPort(int ddr, int offset, int port) {
        for (int i = offset; i < offset+8; i++) {
            int mask = 1 << (i%8);
            if ((ddr & mask) == 0) {
                if (pins[i].value) {
                    port |= mask;
                } else {
                    int maskb = mask ^ 0xFF;
                    port &= maskb;
                }
            } else {
               pins[i].value = (port & mask) != 0;
            }
        }
        return port;
    }


    @Override
    public void next(final int cycles) {
        int ddra = registers[DDRA];
        int ddrb = registers[DDRB];

        porta = PortManipulator.combineDdrOutIn(ddra, registers[PORTA], porta);
        portb = PortManipulator.combineDdrOutIn(ddrb, registers[PORTB], portb);

        porta = processPort(ddra, 0, porta);
        portb = processPort(ddrb, 8, portb);
    }

    @Override
    public void draw(final Graphics graphics) {
        int ddra = registers[DDRA];
        int ddrb = registers[DDRB];

        for (int i = 0; i < 8; i++) {
            int bit = 1 << (7-i); // Draw MSB.....LSB

            // DDR A
            img.setRGB(i*2,0, (ddra & bit) != 0 ? COLOR_DDROUT : COLOR_DDRIN);
            // DDR B
            img.setRGB(i*2+18,0, (ddrb & bit) != 0 ? COLOR_DDROUT : COLOR_DDRIN);

            // ORA A
            img.setRGB(i*2, 2, getPortColor(0xFF, registers[PORTA], bit));
            // ORA B
            img.setRGB(i*2+18,2, getPortColor(0xFF, registers[PORTB], bit));


            // PORT A
            img.setRGB(i*2, 4, getPortColor(ddra, porta, bit));
            // PORT B
            img.setRGB(i*2+18,4, getPortColor(ddrb, portb, bit));
        }

        Graphics2D g2 = ((Graphics2D)graphics);
        g2.drawImage(img, 0,24, W, 36, null);
        g2.setFont(new Font("Courier", Font.PLAIN, 16));
        g2.setColor(Color.WHITE);
        g2.drawString("VIA", 0,16);
        g2.drawString("A", 64, 72);
        g2.drawString("B", 224, 72);

    }
}
