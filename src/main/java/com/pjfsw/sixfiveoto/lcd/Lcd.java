package com.pjfsw.sixfiveoto.lcd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Connectable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Resettable;
import com.pjfsw.sixfiveoto.addressables.via.Pin;

public class Lcd implements Resettable, Clockable, Drawable, Connectable {
    private static final int PIXEL_SIZE = 3;
    private static final int PIXEL_DISTANCE = PIXEL_SIZE;
    private static final int DISPLAY_OFFSET = 8;
    private static final int DISPLAY_PADDING = 2;
    private static final int CHARACTER_OFFSET = DISPLAY_OFFSET + DISPLAY_PADDING;
    private static final int CHARACTER_X_SPACING = 6 * PIXEL_DISTANCE;
    private static final int CHARACTER_Y_SPACING = 9 * PIXEL_DISTANCE;

    private static final Color NO_CHAR_COLOR = new Color(0.0f,0.0f,0.0f,0.05f);

    private final Pin rs = Pin.input();
    private final Pin e = Pin.input();
    private final Pin[] data = new Pin[8];
    private final Pin rw = Pin.input();
    private final int clockSpeed;

    private static final Pattern DATA_RE = Pattern.compile("d([0-7])");
    private final int rows;
    private final int cols;
    private final LcdFont lcdFont = new LcdFont();


    private final BufferedImage[] font = new BufferedImage[128];

    public Lcd(int clockSpeed, int cols, int rows) {
        this.clockSpeed = clockSpeed;
        this.cols = cols;
        this.rows = rows;
        reset(true);
    }
    @Override
    public void next(final int cycles) {

    }


    private void createCharacter(int code) {
        font[code] = new BufferedImage(PIXEL_DISTANCE * 5, PIXEL_DISTANCE * 8, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = (Graphics2D)font[code].getGraphics();
        int[] fontData = lcdFont.getFontData();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 5; x++) {
                if ((fontData[(code * 8+y) % fontData.length] & (1 << (4-x))) != 0) {
                    g2.setColor(Color.BLACK);
                } else {
                    g2.setColor(NO_CHAR_COLOR);
                }
                g2.fillRect(
                    x * PIXEL_DISTANCE, y * PIXEL_DISTANCE,
                    PIXEL_SIZE, PIXEL_SIZE);
            }
        }
    }

    @Override
    public void reset(final boolean hardReset) {
        if (!hardReset) {
            return;
        }

        for (int i = 0; i < 8; i++) {
            data[i] = Pin.input();
        }

        for (int i = 0; i < 128; i++) {
            createCharacter(i);
        }
    }

    @Override
    public Pin getPin(final String pinName) {
        Matcher m;

        if (pinName.equalsIgnoreCase("e")) {
            return e;
        } else if (pinName.equalsIgnoreCase("rs")) {
            return rs;
        } else if (pinName.equalsIgnoreCase("rw")) {
            return rw;
        } else if ((m = DATA_RE.matcher(pinName)).matches()) {
            int dataPin = Integer.parseInt(m.group(1));
            return data[dataPin];
        }
        return null;
    }

    @Override
    public void draw(final Graphics graphics) {
        Graphics2D g2 = (Graphics2D)graphics;
        g2.setColor(new Color(0x333333));

        g2.fillRect(0,0, 2*CHARACTER_OFFSET+cols*CHARACTER_X_SPACING, 2*CHARACTER_OFFSET+rows*CHARACTER_Y_SPACING);
        g2.setColor(new Color(0x99bb55));
        g2.fillRect(DISPLAY_OFFSET,DISPLAY_OFFSET, cols*CHARACTER_X_SPACING, rows*CHARACTER_Y_SPACING);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                g2.drawImage(font[(y*cols+x)%128], CHARACTER_OFFSET+x*CHARACTER_X_SPACING, CHARACTER_OFFSET+y*CHARACTER_Y_SPACING, null);
            }
        }
    }


}
