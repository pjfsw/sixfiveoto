package com.pjfsw.sixfiveoto.picogfx;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.URISyntaxException;
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

    private static final int VISIBLE_HEIGHT=296;
    private static final int MEM_MASK = 0x1FFFF;

    private static final int REG_D = 0;
    private static final int REG_PAGE = 1;
    private static final int REG_AL = 2;
    private static final int REG_AH = 3;

    private static final int SPRITE_LEFT_OFFSET = -16;
    private static final int SPRITE_TOP_OFFSET = -112;
    private static final int SPRITE_RIGHT_EDGE = 432;
    private static final int NUMBER_OF_SPRITES = 12;
    private static final int BITMAP_WIDTH_BYTES = 200;
    private static final int NUMBER_OF_FONTS = 2;

    private final int cyclesPerFrame;
    private final Tile[][] tiles = new Tile[NUMBER_OF_FONTS][256];
    private final int[] registers = new int[MEM_MASK+1];

    private static final int SCREEN_OFFSET = 0x0000;
    private static final int COLOUR_OFFSET = 0x1000;

    private static final int SCREEN0_OFFSET = 0x00000;
    private static final int SCREEN1_OFFSET = 0x02000;
    private static final int FONT_OFFSET    = 0x04000; // two fonts
    private static final int SCREEN_PALETTE = 0x06000;
    private static final int SPRITE_X       = 0x06400;
    private static final int SPRITE_Y       = 0x06420;
    private static final int SPRITE_H       = 0x06440;
    private static final int SPRITE_PTR     = 0x06450;
    private static final int SCROLL_Y       = 0x06460;
    private static final int SCROLL_X       = 0x06462;
    private static final int SCREEN_SELECT  = 0x06464;
    private static final int BITMAP_START   = 0x0646a;
    private static final int BITMAP_HEIGHT  = 0x0646c;
    private static final int BITMAP_PTR     = 0x0646e;
    private static final int BITMAP_PALETTE = 0x06470;
    private static final int FONT_SELECT    = 0x06480;
    private static final int SPRITE_DATA    = 0x10000;


    private final int[] font;
    private final BufferedImage img;
    private int writePtr = 0;
    private int cycles;
    private static final int COLORS = 64;
    private static final int COLOR_MASK = COLORS-1;
    private final Rgba[] colorDac = new Rgba[COLORS];
    private static final int[] COLOR_VALUES_2BIT = {0,85,170,255};
    private boolean irq;

    public PicoGfx(int systemSpeed, int[] font)  {
        img = new BufferedImage(VISIBLE_WIDTH, VISIBLE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        this.cyclesPerFrame = systemSpeed/60;
        this.font = font;
        init();
    }

    private void init_test() {
        int c=0;
        for (int i = 0; i < 256; i++) {
            registers[SCREEN_PALETTE+i*4] = (c)&63;
            registers[SCREEN_PALETTE+i*4+1] = (c+1)&63;
            registers[SCREEN_PALETTE+i*4+2] = (c+2)&63;
            registers[SCREEN_PALETTE+i*4+3] = (c+3)&63;
            c++;
        }
        char[] message = "Double buffering ftw! ".toCharArray();
        int ch = 32;
        int chPos = 0;
        for (int i = 0; i < 4096; i++) {
            registers[SCREEN0_OFFSET + COLOUR_OFFSET+i] = i&255;
            registers[SCREEN0_OFFSET + SCREEN_OFFSET+i] = ch++;
            if (ch > 254) {
                ch = 32;
            }
            registers[SCREEN1_OFFSET + COLOUR_OFFSET+i] = 0;
            registers[SCREEN1_OFFSET + SCREEN_OFFSET+i] = message[chPos];
            chPos = (chPos+1) % message.length;
        }
    }

    private void init() {
        cycles = 0;
        Arrays.fill(registers, 0);
        System.arraycopy(font, 0, registers, FONT_OFFSET, font.length);
        writePtr = 0;
        for (int font = 0; font < NUMBER_OF_FONTS; font++) {
            for (int i = 0; i < 256; i++) {
                createTile(font, i);
            }
        }
        for (int i = 0; i < COLORS; i++) {
            int r = (i>>4) & 3;
            int g = (i>>2) & 3;
            int b = i & 3;
            colorDac[i] = new Rgba(COLOR_VALUES_2BIT[r], COLOR_VALUES_2BIT[g], COLOR_VALUES_2BIT[b],255);
        }
        init_test();
    }

    private void createTile(int font, int i) {
        int fontOffset = font * 0x1000;
        Tile tile = new Tile();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int ramPos = i * 16 + y * 2 + x / 4;
                int charData = registers[fontOffset + FONT_OFFSET + ramPos];
                int bitPos = (3-(x%4))*2; // 6, 4, 2, 0
                int bitMask = (1 << (bitPos+1)) | (1 << bitPos); // 192, 48, 12, 3
                int colorIndex = (charData & bitMask) >> bitPos;
                if (colorIndex > 0) {
                    tile.set(x,y, colorIndex);
                }
            }
        }
        tiles[font][i] = tile;
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

    private void drawTiles(WritableRaster raster, int y, int scrollX, int scrollY) {
        int screenOffset = (registers[SCREEN_SELECT] & 1) << 13;
        int sy = (y + scrollY) & 0x1ff;
        int sy3 = sy>>3;
        int font = registers[FONT_SELECT+sy3] % NUMBER_OF_FONTS;
        for (int x = 0; x < VISIBLE_WIDTH; x++) {
            int sx = (x + scrollX) & 0x1ff;
            int screenPos = screenOffset + (sy3 * CHARS_PER_LINE) + (sx>>3);
            int tileIndex = registers[SCREEN_OFFSET + screenPos];
            Tile tile = tiles[font][tileIndex & 0xFF];
            int color = tile.get(sx&7,sy&7);
            int palette = registers[COLOUR_OFFSET + screenPos];
            int colorIndex = registers[SCREEN_PALETTE + palette * 4 + color];
            raster.setPixel(x,y, colorDac[colorIndex & COLOR_MASK].rgba);
        }
    }

    private int getBitmapPtr() {
        return registers[BITMAP_PTR] + (registers[BITMAP_PTR+1] << 8);
    }
    private void drawBitmap(WritableRaster raster, int y, int bitmapStart) {
        int bitmapPtr = getBitmapPtr();
        int bitmapRow = (y - bitmapStart - 512) & 0x1ff;
        int bitmapOffset = ((bitmapPtr << 1) + bitmapRow * BITMAP_WIDTH_BYTES);
        for (int x = 0; x < VISIBLE_WIDTH; x+=2) {
            int c = registers[bitmapOffset & 0x1ffff];
            for (int i = 0; i < 2; i++) {
                int shift = 4 - 4 * i;
                int color = (c >> shift) & 15;
                int colorIndex = registers[BITMAP_PALETTE + color];
                raster.setPixel(x+i, y, colorDac[colorIndex & COLOR_MASK].rgba);
            }
            bitmapOffset++;
        }

    }

    private void drawSprites(WritableRaster raster, int y) {
        for (int i = 0; i < NUMBER_OF_SPRITES; i++) {
            int spritePos = y - registers[SPRITE_Y + i * 2] - (registers[SPRITE_Y + i * 2 + 1] << 8) - SPRITE_TOP_OFFSET;
            if (spritePos < 0 || spritePos >= registers[SPRITE_H]) {
                continue;
            }
            int spriteOffset = (spritePos << 4) + (registers[SPRITE_PTR + i] << 8);
            int xpos = SPRITE_LEFT_OFFSET + registers[SPRITE_X + i * 2] + (registers[SPRITE_X + i * 2 + 1] << 8);
            int n = Math.min(16, SPRITE_RIGHT_EDGE-xpos);
            for (int x = 0; x < n; x++) {
                int color = registers[SPRITE_DATA+(spriteOffset&0xFFFF)];
                if (xpos >= 0 && xpos < VISIBLE_WIDTH && (color & 0x80) == 0) {
                    raster.setPixel(xpos, y, colorDac[color & COLOR_MASK].rgba);
                }
                xpos++;
                spriteOffset++;
            }
        }
    }

    @Override
    public void draw(Graphics graphics) {
        Graphics2D g2 = (Graphics2D)graphics;
        WritableRaster raster = img.getRaster();
        int scrollX = registers[SCROLL_X] + ((registers[SCROLL_X+1] & 1) << 8);
        int scrollY = registers[SCROLL_Y] + ((registers[SCROLL_Y+1] & 1) << 8);

        int bitmapStart = registers[BITMAP_START] + (registers[BITMAP_START+1] << 8)-512;
        int bitmapHeight = registers[BITMAP_HEIGHT] + (registers[BITMAP_HEIGHT+1] << 8);
        /*if (bitmapStart > 0) {
            System.out.printf("BitmapPtr = %04x, BitmapStart = %04x, BitmapHeight = %04x%n",
                getBitmapPtr(), bitmapStart, bitmapHeight);
        }*/

        for (int y = 0; y < VISIBLE_HEIGHT; y++) {
            if (y >= bitmapStart && y < bitmapStart+bitmapHeight) {
                drawBitmap(raster, y, bitmapStart);
            } else {
                drawTiles(raster, y, scrollX, scrollY);
            }
            drawSprites(raster, y);
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
        registers[writePtr] = data;
        if (writePtr >= FONT_OFFSET && writePtr < FONT_OFFSET + 0x1000 * NUMBER_OF_FONTS) {
            int index = writePtr-FONT_OFFSET;
            createTile(index/0x1000, (index &0xFFF)>>4);
        }
        writePtr = (writePtr + 1) & MEM_MASK;
    }

    @Override
    public void poke(int address, int data) {
        address = address & 7;
        irq = false;
        switch (address) {
            case REG_D:
                writeScreen(data);
                break;
            case REG_AL:
                writePtr = (writePtr & 0x1FF00) | data;
                break;
            case REG_AH:
                writePtr = (writePtr & 0x100FF) | (data<<8);
                break;
            case REG_PAGE:
                writePtr = (writePtr & 0x0FFFF) | ((data&1)<<16);
                //writePtr = (writePtr + (data & 0x7F)) & MEM_MASK;
                break;
            default:
        }
    }

    private static class Tile {
        int[] data = new int[64];

        void set(int x, int y, int color) {
            data[(y*8+x)&63] = color;
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
