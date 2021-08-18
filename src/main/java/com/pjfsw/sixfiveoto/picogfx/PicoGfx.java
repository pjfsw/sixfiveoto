package com.pjfsw.sixfiveoto.picogfx;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Poker;
import com.pjfsw.sixfiveoto.addressables.Resettable;

public class PicoGfx implements Drawable, Clockable, Resettable, Poker {
    private static final int VISIBLE_WIDTH=320;
    private static final int MEMORY_WIDTH=512;
    private static final int CHARS_PER_LINE = MEMORY_WIDTH/8;
    private static final int LINE_MASK = 63;

    private static final int VISIBLE_HEIGHT=240;
    private static final int MEMORY_HEIGHT=256;
    private static final int CHARS_PER_COL = MEMORY_HEIGHT/8;
    private static final int WRITE_PAGE_SIZE = CHARS_PER_LINE * CHARS_PER_COL;
    private static final int MEM_MASK = WRITE_PAGE_SIZE - 1;

    private static final int REG_D = 0;
    private static final int REG_AY = 1;
    private static final int REG_AX = 2;
    private static final int REG_PAGE = 3;
    private static final int REG_LENGTH = 4;
    private static final int REG_SKIP = 5;
    private static final int REG_BG = 6;

    private final int cyclesPerFrame;
    private final Tile[] tiles = new Tile[256];
    private final int[] registers = new int[65536];
    private static final int TILE_OFFSET = 0x2000;
    private static final int TILE_COLOR_OFFSET = 0x0800;
    private final int[] font;
    private int writePtr = 0;
    private int bgColor;
    private int writePage = 0;
    private int writeLength = 1;
    private int writeSkip = 0;
    private int writeCounter = 0;
    private final Color[] colorDac = new Color[64];
    private static final int[] COLOR_VALUES = {0,85,170,255};

    public PicoGfx(int systemSpeed, int[] font) {
        this.cyclesPerFrame = systemSpeed/60;
        this.font = font;
        init();
    }

    private void init() {
        Arrays.fill(registers, 0);
        System.arraycopy(font, 0, registers, TILE_OFFSET, font.length);
        writePtr = 0;
        for (int i = 0; i < 256; i++) {
            createTile(i);
        }
        for (int i = 0; i < 64; i++) {
            int r = (i>>4) & 3;
            int g = (i>>2) & 3;
            int b = i & 3;
            colorDac[i] = new Color(COLOR_VALUES[r], COLOR_VALUES[g], COLOR_VALUES[b]);
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

    }

    @Override
    public void draw(Graphics graphics) {
        Graphics2D g2 = (Graphics2D)graphics;
        g2.setBackground(colorDac[bgColor & 63]);
        g2.clearRect(0, 0, VISIBLE_WIDTH*2, VISIBLE_HEIGHT*2);

        for (int y = 0; y < VISIBLE_HEIGHT; y++) {
            for (int x = 0; x < VISIBLE_WIDTH; x++) {
                int screenPos = ((y>>3)*CHARS_PER_LINE) + (x>>3);
                int tileIndex = registers[screenPos];
                Tile tile = tiles[tileIndex & 0xFF];
                int colorIndex = registers[TILE_COLOR_OFFSET+screenPos];
                if (tile.get(x&7,y&7) > 0) {
                    g2.setColor(colorDac[colorIndex&63]);
                    g2.fillRect(x*2,y*2,2,2);
                }
            }
        }

    }

    @Override
    public void reset(boolean hardReset) {
        if (hardReset) {
            init();
        }
    }

    private void writeScreen(int data) {
        registers[(writePage & 1) * WRITE_PAGE_SIZE + (writePtr & MEM_MASK)] = data;
        writePtr = (writePtr + 1);
        writeCounter++;
        if (writeCounter >= writeLength+1) {
            writePtr += writeSkip;
            writeCounter = 0;
        }
        writePtr &= MEM_MASK;
    }

    @Override
    public void poke(int address, int data) {
        address = address & 7;
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

}
