package com.pjfsw.sixfiveoto.peripherals;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.function.Consumer;

import com.pjfsw.sixfiveoto.addressables.Drawable;

public class Led implements Consumer<Boolean>, Drawable {
    private static final int SIZE = 16;
    private final Color onColor;
    private final Color offColor;
    private boolean on;

    public static Led green() {
        return new Led(new Color(0,255,0), new Color(0,63,0));
    }

    public Led(Color onColor, Color offColor) {
        this.onColor = onColor;
        this.offColor = offColor;
    }

    @Override
    public void accept(final Boolean on) {
        this.on = on;
    }

    @Override
    public void draw(final Graphics graphics) {
        Graphics2D g2 = ((Graphics2D)graphics);
        if (on) {
            g2.setColor(onColor);
        } else {
            g2.setColor(offColor);
        }
        g2.fillOval(0,0,SIZE,SIZE);
        g2.setColor(Color.WHITE);
        g2.fillOval(SIZE/4,SIZE/4,SIZE/4, SIZE/4);
    }
}
