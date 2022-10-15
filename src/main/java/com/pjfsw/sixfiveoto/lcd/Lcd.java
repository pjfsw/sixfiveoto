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
    private static final int DISPLAY_PADDING = 4;
    private static final int CHARACTER_OFFSET = DISPLAY_OFFSET + DISPLAY_PADDING;
    private static final int CHARACTER_X_SPACING = 6 * PIXEL_DISTANCE;
    private static final int CHARACTER_Y_SPACING = 9 * PIXEL_DISTANCE;

    private static final Color NO_CHAR_COLOR = new Color(0.0f,0.0f,0.0f,0.03f);

    private final Pin rs = Pin.input();
    private final Pin e = Pin.input();
    private final Pin e2 = Pin.input();
    private final Pin[] data = new Pin[8];
    private final Pin rw = Pin.input();

    private static final Pattern DATA_RE = Pattern.compile("d([0-7])");
    private final int rows;
    private final int cols;
    private final LcdFont lcdFont = new LcdFont();

    private final LcdController[] controllers = new LcdController[2];

    private final BufferedImage[] font = new BufferedImage[96];
    private static final int[] rowOffsets = {0, 0x40, 0x14, 0x54};

    public Lcd(int clockSpeed, int cols, int rows) {
        this.cols = cols;
        this.rows = rows;
        for (int i = 0; i < 8; i++) {
            data[i] = Pin.input();
        }
        reset(true);
    }
    @Override
    public void next(final int cycles) {
        controllers[0].update(cycles);
        controllers[1].update(cycles);
    }


    private void createCharacter(int code) {
        int fontIndex = code - 32;
        font[fontIndex] = new BufferedImage(PIXEL_DISTANCE * 5, PIXEL_DISTANCE * 8, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = (Graphics2D)font[fontIndex].getGraphics();
        int[] fontData = lcdFont.getFontData();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 5; x++) {
                if ((fontData[(fontIndex * 8+y) % fontData.length] & (1 << (4-x))) != 0) {
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

    private int getController(int row) {
        if (cols == 40 && rows == 4 && row > 1) {
            return 1;
        }
        return 0;
    }

    private int getRowOffset(int row) {
        if (cols == 40) {
            return (row * 64) % 128;
        } else {
            return rowOffsets[row];
        }
    }

    @Override
    public void reset(final boolean hardReset) {
        if (!hardReset) {
            return;
        }

        controllers[0] = new LcdController(e, rs, rw, data);
        controllers[1] = new LcdController(e2, rs, rw, data);

        for (int i = 32; i < 128; i++) {
            createCharacter(i);
        }
    }

    @Override
    public Pin getPin(final String pinName) {
        Matcher m;

        if (pinName.equalsIgnoreCase("e")) {
            return e;
        } else if (pinName.equalsIgnoreCase("e2")) {
            return e2;
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
        g2.fillRect(
            DISPLAY_OFFSET,DISPLAY_OFFSET,
            DISPLAY_PADDING*2+cols*CHARACTER_X_SPACING, DISPLAY_PADDING*2+rows*CHARACTER_Y_SPACING
        );
        for (int y = 0; y < rows; y++) {
            int rowOffset = getRowOffset(y);
            int controller = getController(y);
            for (int x = 0; x < cols; x++) {
                int chr = controllers[controller].getCharAt(rowOffset+x);
                if (chr < 32 || chr > 127) {
                    chr = 32;
                }
                g2.drawImage(font[chr-32], CHARACTER_OFFSET+x*CHARACTER_X_SPACING, CHARACTER_OFFSET+y*CHARACTER_Y_SPACING, null);
            }
        }
    }

    @Override
    public int getHeight() {
        return DISPLAY_OFFSET + rows * CHARACTER_Y_SPACING + DISPLAY_PADDING * 2;
    }

    @Override
    public int getWidth() {
        return DISPLAY_OFFSET + DISPLAY_PADDING*2+cols*CHARACTER_X_SPACING;
    }

}
