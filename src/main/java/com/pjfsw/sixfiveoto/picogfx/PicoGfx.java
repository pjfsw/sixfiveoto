package com.pjfsw.sixfiveoto.picogfx;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private final int cyclesPerFrame;
    private final Tile[] tiles = new Tile[256];
    private final int[] registers = new int[MEM_MASK+1];

    private static final int SCREEN_OFFSET = 0x00000;
    private static final int COLOUR_OFFSET = 0x08000;
    private static final int FONT_OFFSET = 0xc000;
    private static final int SCROLL_Y = 0xd030;
    private static final int SCROLL_X = 0xd032;
    private static final int BITMAP_START = 0xd034;
    private static final int BITMAP_HEIGHT = 0xd036;
    private static final int BITMAP_PTR = 0xd038;
    private static final int BITMAP_PALETTE = 0xd03a;

    private final int[] font;
    private final BufferedImage img;
    private int writePtr = 0;
    private int cycles;
    private static final int COLORS = 64;
    private static final int COLOR_MASK = COLORS-1;
    private final Rgba[] colorDac = new Rgba[COLORS];
    private static final int[] COLOR_VALUES_2BIT = {0,85,170,255};
    private boolean irq;


    /*
        Memory map:
        ===========

        $00000-$07fff : Screen RAM 32K
        $08000-$0bfff : Colour RAM 16K
        $0c000-$0cfff : 4 colour Font 4K
        $0d000-$0d00f : 8 x Sprite X (16-bit L.E.)
        $0d010-$0d01f : 8 x Sprite Y (16-bit L.E.)
        $0d020-$0d027 : 8 x Sprite height (8-bit)
        $0d028-$0d02f : 8 x Sprite pointer (page selector in sprite memory)
        $0d030-$0d031 : Scroll Y
        $0d032-$0d033 : Scroll X
        $0d034-$0d035 : Bitmap start, 0-1023, 512 = first visible row
        $0d036-$0d037 : Bitmap height
        $0d038-$0d039 : Bitmap pointer represented as offset in 16-bit words from GFX base
        $0d03a-$0d03d : Bitmap palette (4 colours)
        $10000-$1ffff : Sprite RAM

     */
    public PicoGfx(int systemSpeed, int[] font)  {
        img = new BufferedImage(VISIBLE_WIDTH, VISIBLE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        this.cyclesPerFrame = systemSpeed/60;
        this.font = font;
        init();
    }

    private void init_test() {
        int c = 0;
        int r = 0b110000;
        int g = 0b001100;
        int b = 0b000011;
        int blk = 0;

        for (int i = 0; i < 4096; i++) {
            registers[COLOUR_OFFSET+i*4] = (i>>5)&63;
            registers[COLOUR_OFFSET+i*4+1] = r;
            registers[COLOUR_OFFSET+i*4+2] = g;
            registers[COLOUR_OFFSET+i*4+3] = b;
        }
        int ch = 32;
        for (int i = 0; i < 4096; i++) {
            registers[SCREEN_OFFSET+i] = ch++;
            if (ch > 254) {
                ch = 32;
            }
        }
        try {
            byte[] picBytes = Files.readAllBytes(
                new File(getClass().getResource("derp_pixels.bin").toURI()).toPath());
            int n = 0;
            for (int y = 0; y < 220; y++) {
                for (int x = 0; x < 280/4; x++) {
                    registers[0x10000+y*100+x] = picBytes[n];
                    n++;
                }
            }
            registers[BITMAP_PTR] = 0;
            registers[BITMAP_PTR+1] = 0x80;
            registers[BITMAP_START] = 0;
            registers[BITMAP_START+1] = 2;
            registers[BITMAP_HEIGHT] = 220;
            registers[BITMAP_HEIGHT+1] = 0;
            registers[BITMAP_PALETTE] = 0;
            registers[BITMAP_PALETTE+1] = 0x15;
            registers[BITMAP_PALETTE+2] = 0x2a;
            registers[BITMAP_PALETTE+3] = 0x3f;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }

    }
    private void init() {
        cycles = 0;
        Arrays.fill(registers, 0);
        System.arraycopy(font, 0, registers, FONT_OFFSET, font.length);
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
        init_test();
    }

    private void createTile(int i) {
        Tile tile = new Tile();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int ramPos = i * 16 + y * 2 + x / 4;
                int charData = registers[FONT_OFFSET + ramPos];
                int bitPos = (3-(x%4))*2; // 6, 4, 2, 0
                int bitMask = (1 << (bitPos+1)) | (1 << bitPos); // 192, 48, 12, 3
                int colorIndex = (charData & bitMask) >> bitPos;
                if (colorIndex > 0) {
                    tile.set(x,y, colorIndex);
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

    private void drawTiles(WritableRaster raster, int y, int scrollX, int scrollY) {
        for (int x = 0; x < VISIBLE_WIDTH; x++) {
            int sx = (x + scrollX) & 0x1ff;
            int sy = (y + scrollY) & 0x1ff;
            int screenPos = ((sy>>3) * CHARS_PER_LINE) + (sx>>3);
            int tileIndex = registers[SCREEN_OFFSET + screenPos];
            Tile tile = tiles[tileIndex & 0xFF];
            int color = tile.get(sx&7,sy&7);
            int colorIndex = registers[COLOUR_OFFSET + screenPos * 4 + color];
            raster.setPixel(x,y, colorDac[colorIndex & COLOR_MASK].rgba);
        }
    }

    private void drawBitmap(WritableRaster raster, int y, int bitmapStart) {
        int bitmapPtr = registers[BITMAP_PTR] + registers[BITMAP_PTR+1] << 8;
        int bitmapRow = (y - bitmapStart - 512) & 0x1ff;
        int bitmapOffset = ((bitmapPtr << 1) + bitmapRow * 100);
        for (int x = 0; x < VISIBLE_WIDTH; x+=4) {
            int c = registers[bitmapOffset & 0x1ffff];
            for (int i = 0; i < 4; i++) {
                int shift = 6 - 2 * i;
                int color = (c >> shift) & 3;
                int colorIndex = registers[BITMAP_PALETTE + color];
                raster.setPixel(x+i, y, colorDac[colorIndex & COLOR_MASK].rgba);
            }
            bitmapOffset++;
        }

    }

    @Override
    public void draw(Graphics graphics) {
        Graphics2D g2 = (Graphics2D)graphics;
        WritableRaster raster = img.getRaster();
        int scrollX = registers[SCROLL_X] + ((registers[SCROLL_X+1] & 1) << 8);
        int scrollY = registers[SCROLL_Y] + ((registers[SCROLL_Y+1] & 1) << 8);
        int bitmapStart = (registers[BITMAP_START] + registers[BITMAP_START+1] << 8)-512;
        int bitmapHeight = registers[BITMAP_HEIGHT] + registers[BITMAP_HEIGHT+1] << 8;
        for (int y = 0; y < VISIBLE_HEIGHT; y++) {
            if (y < 220) {
                drawBitmap(raster, y, bitmapStart);
            } else {
                drawTiles(raster, y, scrollX, scrollY);
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
        registers[writePtr] = data;
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
                writePtr = (writePtr & 0x0FFFF) | ((data&0x80)<<9);
                writePtr = (writePtr + (data & 0x7F)) & MEM_MASK;
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
