package com.pjfsw.sixfiveoto.addressables.via;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;

public class Via6522 implements Peeker, Poker, Drawable {
    private final BufferedImage img;
    private static final int INPUT_COLOR = 0xFF00FF00;
    private static final int OUTPUT_COLOR = 0x7700FF00;

    public Via6522() {
        img = new BufferedImage(32,4, TYPE_INT_ARGB);
        for (int i = 0; i < img.getWidth()/2; i++) {
            img.setRGB(i*2,0,(i % 2 == 0) ? Color.WHITE.getRGB() : Color.GRAY.getRGB());
            img.setRGB(i*2,2,(i % 3 == 0) ? INPUT_COLOR : OUTPUT_COLOR);
        }
/*        for (Font font : GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts()) {
            System.out.println(font.getName());
        }*/
    }
    @Override
    public int peek(final int address) {
        return 0;
    }

    @Override
    public void poke(final int address, final int data) {

    }

    @Override
    public void draw(final Graphics graphics) {
        Graphics2D g2 = ((Graphics2D)graphics);
        g2.translate(320,1);
        g2.drawImage(img, 0,24, 320, 32, null);
        g2.setFont(new Font("Monospaced", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("65C22", 0,16);

    }
}
