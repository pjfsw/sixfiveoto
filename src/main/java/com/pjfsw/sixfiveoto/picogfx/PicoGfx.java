package com.pjfsw.sixfiveoto.picogfx;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Interrupt;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.addressables.Resettable;

public class PicoGfx implements Drawable, Clockable, Resettable, Poker, Interrupt {
    private static final int VISIBLE_WIDTH=400;
    private static final int MEMORY_WIDTH=512;
    private static final int CHARS_PER_LINE = MEMORY_WIDTH/8;
    private static final int LINE_MASK = CHARS_PER_LINE-1;

    private static final int VISIBLE_HEIGHT=296;
    private static final int WRITE_PAGE_SIZE = 0x1000;
    private static final int MEM_MASK = WRITE_PAGE_SIZE - 1;

    private static final int REG_D = 0;
    private static final int REG_AY = 1;
    private static final int REG_AX = 2;
    private static final int REG_PAGE = 3;
    private static final int REG_LENGTH = 4;
    private static final int REG_SKIP = 5;
    private static final int REG_BG = 6;
    private static final int REG_CTRL = 7;
    private static final int CTRL_SAVE_AXAY = 0;
    private static final int CTRL_RESTORE_AXAY = 1;

    private final int cyclesPerFrame;
    private final Tile[] tiles = new Tile[256];
    private final int[] registers = new int[65536];
    private static final int TILE_OFFSET = 0x3000;
    private static final int TILE_COLOR_OFFSET = 0x1000;
    private static final int CTRL_OFFSET = 0x2000;
    private static final int CTRL_SCRX = CTRL_OFFSET;
    private static final int CTRL_SCRY = CTRL_OFFSET+2;

    private final int[] font;
    private final BufferedImage img;
    private int writePtr = 0;
    private int bgColor;
    private int writePage = 0;
    private int writeLength = 1;
    private int writeSkip = 0;
    private int writeCounter = 0;
    private int cycles;
    private static final int COLORS = 64;
    private static final int COLOR_MASK = COLORS-1;
    private final Rgba[] colorDac = new Rgba[COLORS];
    private static final int[] COLOR_VALUES_2BIT = {0,85,170,255};
    //private static final int[] COLOR_VALUES_2BIT = {0,109,182,255};
    //private static final int[] COLOR_VALUES_3BIT = {0,36,73,109,146,182,219,255};
    private boolean irq;
    private int writePtrBackup;

    public PicoGfx(int systemSpeed, int[] font) {
        img = new BufferedImage(VISIBLE_WIDTH, VISIBLE_HEIGHT, BufferedImage.TYPE_INT_ARGB);

        this.cyclesPerFrame = systemSpeed/60;
        this.font = font;
        init();
    }

    private void init() {
        cycles = 0;
        Arrays.fill(registers, 0);
        System.arraycopy(font, 0, registers, TILE_OFFSET, font.length);
        writePtr = 0;
        for (int i = 0; i < 256; i++) {
            createTile(i);
        }
        for (int i = 0; i < COLORS; i++) {
            int r = (i>>4) & 3;
            int g = (i>>2) & 3;
            int b = i & 3;
            colorDac[i] = new Rgba(COLOR_VALUES_2BIT[r], COLOR_VALUES_2BIT[g], COLOR_VALUES_2BIT[b],255);
        }
    }

    private void createTile(int i) {
        Tile tile = new Tile();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int ramPos = i * 16 + y * 2 + x / 4;
                int charData = registers[TILE_OFFSET + ramPos];
                int bitPos = (3-(x%4))*2; // 6, 4, 2, 0
                int bitMask = (1 << (bitPos+1)) | (1 << bitPos); // 192, 48, 12, 3
                int colorIndex = (charData & bitMask) >> bitPos;
                if (colorIndex > 0) {
                    tile.set(x,y);
                }
            }
        }
        tiles[i] = tile;
    }

    @Override
    public void next(int cycles) {
        this.cycles += cycles;
        if (this.cycles >= cyclesPerFrame) {
            vbl();
            this.cycles -= cyclesPerFrame;
        }
    }

    private void vbl() {
        irq = true;
    }

    public boolean hasIrq() {
        return irq;
    }

    @Override
    public void draw(Graphics graphics) {
        Graphics2D g2 = (Graphics2D)graphics;
        WritableRaster raster = img.getRaster();

        int scrollX = registers[CTRL_SCRX] + ((registers[CTRL_SCRX+1] & 1) << 8);
        int scrollY = registers[CTRL_SCRY] + ((registers[CTRL_SCRY+1] & 1) << 8);
        for (int y = 0; y < VISIBLE_HEIGHT; y++) {
            for (int x = 0; x < VISIBLE_WIDTH; x++) {
                int sx = (x + scrollX) & 0x1ff;
                int sy = (y + scrollY) & 0x1ff;
                int screenPos = ((sy>>3)*CHARS_PER_LINE) + (sx>>3);
                int tileIndex = registers[screenPos];
                Tile tile = tiles[tileIndex & 0xFF];
                int colorIndex = registers[TILE_COLOR_OFFSET+screenPos];
                int bit = tile.get(sx&7,sy&7);
                if (colorIndex > 127) {
                    bit = 1-bit;
                }
                if (bit > 0) {
                    raster.setPixel(x,y, colorDac[colorIndex & COLOR_MASK].rgba);
                } else {
                    raster.setPixel(x,y, colorDac[bgColor & COLOR_MASK].rgba);
                }
            }
        }

        g2.drawImage(img, 0,0, VISIBLE_WIDTH*2, VISIBLE_HEIGHT*2, null);

    }

    @Override
    public void reset(boolean hardReset) {
        if (hardReset) {
            init();
        }
    }

    private void writeScreen(int data) {
        registers[(writePage & 3) * WRITE_PAGE_SIZE + (writePtr & MEM_MASK)] = data;
        writePtr = (writePtr + 1);
        writeCounter++;
        if (writeCounter >= writeLength+1) {
            writePtr += writeSkip;
            writeCounter = 0;
        }
        writePtr &= MEM_MASK;
    }

    private void handleCtrl(int data) {
        switch(data) {
            case CTRL_SAVE_AXAY:
                writePtrBackup = writePtr;
                break;
            case CTRL_RESTORE_AXAY:
                writePtr = writePtrBackup;
                break;
            default:
        }
    }
    @Override
    public void poke(int address, int data) {
        address = address & 7;
        irq = false;
        switch (address) {
            case REG_D:
                writeScreen(data);
                break;
            case REG_AY:
                writeCounter = 0;
                writePtr = (writePtr & 0x003F) | (data * CHARS_PER_LINE);
                break;
            case REG_AX:
                writeCounter = 0;
                writePtr = (writePtr & 0xFFC0) | (data & LINE_MASK);
                break;
            case REG_PAGE:
                writeCounter = 0;
                writePage = data;
                break;
            case REG_BG:
                bgColor = data;
                break;
            case REG_LENGTH:
                writeLength = data;
                break;
            case REG_SKIP:
                writeSkip = data;
                break;
            case REG_CTRL:
                handleCtrl(data);
                break;
            default:
        }
    }

    private static class Tile {
        int[] data = new int[64];

        void set(int x, int y) {
            data[(y*8+x)&63] = 1;
        }

        int get(int x, int y) {
            return data[(y*8+x)&63];
        }
    }

    private static class Rgba {
        int[] rgba;

        public Rgba(int r, int g, int b, int a) {
            this.rgba = new int[]{r,g,b,a};
        }
    }

}
