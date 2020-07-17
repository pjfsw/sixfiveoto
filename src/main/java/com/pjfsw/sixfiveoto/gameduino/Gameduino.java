package com.pjfsw.sixfiveoto.gameduino;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Resettable;
import com.pjfsw.sixfiveoto.spi.Spi;

/**
 * 0x0000-0x0FFF RAM_PIC
 * 0x1000-0x1FFF RAM_CHR
 *    4 pixels per byte 11223344
 * 0x2000-0x27FF RAM_PAL
 *
 */
public class Gameduino implements Drawable, Clockable, Resettable {
    private static final int CANVAS_W = 800;
    private static final int CANVAS_H = 600;
    public static final int W = 802;
    public static final int H = 608;

    private static final int[] spriteXTransforms = new int[] {
        2,-2,
        -2,2,
        2,2,
        -2,-2
    };

    private static final int[] spriteYTransforms = new int[] {
        2,2,
        2,2,
        -2,2,
        -2,2
    };

    private static final double R = Math.asin(1); // 90 degrees CW
    private static final double[] spriteRotations = new double[] {
        0,-R,
        0,-R,
        0,R,
        0,R
    };

    private static final int FRAME = 0x2802;
    private static final int VBLANK = 0x2803;
    private static final int RAM_CHR = 0x1000;
    private static final int RAM_PAL = 0x2000;
    private static final int PALETTE_16A = 0x2840;
    private static final int PALETTE_4A = 0x2880;
    private static final int RAM_SPR = 0x3000;
    private static final int RAM_SPRPAL = 0x3800;
    private static final int RAM_SPRIMG = 0x4000;

    private final Spi spi;
    private final int cyclesPerFrame;
    private final int verticalBlankCycles;
    private final int[] memoryDump;
    private boolean readMode;
    private int address;
    private final int[] registers = new int[32768];
    private int lastPosition;

    private final BufferedImage[] ramChr = new BufferedImage[256];
    private final BufferedImage[] spriteImages = new BufferedImage[256];

    private final int[] fiveBitColors = new int[32];

    private final Set<Integer> ramChrToRebuild = ConcurrentHashMap.newKeySet();
    private final Set<Integer> spriteToRebuild = ConcurrentHashMap.newKeySet();
    private final Set<Integer> sprite4c = ConcurrentHashMap.newKeySet();
    private final Set<Integer> sprite16c = ConcurrentHashMap.newKeySet();
    private int cycles;
    private int framesSinceLastRead;
    private int lastAddress;

    public Gameduino(int systemSpeed, Spi spi, int[] memoryDump) {
        this.cyclesPerFrame = systemSpeed/72;
        // Vertical blank period for 72 Hz is 1.3728 ms = 1372800 nanos
        this.verticalBlankCycles = (int)(((long)systemSpeed/1000L) * 1372800L/1000000L);
        this.spi = spi;
        this.memoryDump = memoryDump;
        for (int i = 0; i < 32; i++) {
            fiveBitColors[i] = 255 * i / 31;
        }
        reset(true);
    }

    private void createRamChr(int i) {
        ramChr[i] = new BufferedImage(8,8, TYPE_INT_ARGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int ramPos = i * 16 + y * 2 + x / 4;
                int charData = registers[RAM_CHR + ramPos];
                int bitPos = (3-(x%4))*2; // 6, 4, 2, 0
                int bitMask = (1 << (bitPos+1)) | (1 << bitPos); // 192, 48, 12, 3
                int colorIndex = (charData & bitMask) >> bitPos;
                int colorValue = getColorRegisterValue(RAM_PAL + i * 8 + colorIndex * 2);
                ramChr[i].setRGB(x,y, colorValue);
            }
        }
    }

    private static int get8BitPalette(long controlData) {
        if ((controlData & 0xC000) == 0) {
            return (int)((controlData >> 12) & 3);
        } else {
            return -1;
        }
    }

    private static int get4BitPalette(long controlData) {
        if ((controlData & 0xC000) == 0x4000) {
            return (int)((controlData >> 12) & 1);
        } else {
            return -1;
        }
    }

    private static int get2BitPalette(long controlData) {
        if ((controlData & 0x8000) == 0x8000) {
            return (int)((controlData >> 12) & 1);
        } else {
            return -1;
        }
    }

    private long getSpriteData(int sprite) {
        return registers[RAM_SPR + sprite * 4]
            + (registers[RAM_SPR + sprite * 4 + 1] << 8)
            + (registers[RAM_SPR + sprite * 4 + 2] << 16)
            + (registers[RAM_SPR + sprite * 4 + 3] << 24);
    }

    private void createSprite(int i) {
        spriteImages[i] = new BufferedImage(16,16, TYPE_INT_ARGB);
        long spriteData = getSpriteData(i);
        int sourceImage = (int)(spriteData >> 25) & 63;

        sprite4c.remove(i);
        sprite16c.remove(i);

        int palette;
        if ((palette = get8BitPalette(spriteData)) != -1) {
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int colorIndex = registers[RAM_SPRIMG + sourceImage  * 256 + y * 16 + x];
                    spriteImages[i].setRGB(x,y, getColorRegisterValue(RAM_SPRPAL +palette * 512 + colorIndex*2));
                }
            }
        } else if ((palette = get4BitPalette(spriteData)) != -1) {
            int nibble = (int)((spriteData >> 13) & 1);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int colorByte = registers[RAM_SPRIMG + sourceImage * 256 + y * 16 + x];
                    int colorIndex = (colorByte >> (4*nibble)) & 15;
                    spriteImages[i].setRGB(x,y, getColorRegisterValue(PALETTE_16A + palette * 32 + colorIndex*2));
                }
            }
            sprite16c.add(i);
        } else {
            palette = get2BitPalette(spriteData);
            int bitPair = (int)((spriteData >> 13) & 3);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int colorByte = registers[RAM_SPRIMG + sourceImage * 256 + y * 16 + x];
                    int colorIndex = (colorByte >> (2*bitPair)) & 3;
                    spriteImages[i].setRGB(x,y, getColorRegisterValue(PALETTE_4A + palette * 8 + colorIndex*2));
                }
            }
            sprite4c.add(i);
        }
    }

    private int getBackgroundColor() {
        return palToRGB(registers[0x280F] * 256 + registers[0x280E]);
    }

    private int palToRGB(int pal) {
        int r = ((pal >> 10) & 0x1f);
        int g = ((pal >> 5) & 0x1f);
        int b = ((pal) & 0x1f);
        int alpha = ((pal & 0x8000) == 0) ? 0xFF000000 : 0;
        return (fiveBitColors[r] << 16) | (fiveBitColors[g] << 8) | fiveBitColors[b] | alpha;
    }

    private int getColorRegisterValue(int register) {
        int lsbValue = registers[register];
        int msbValue = registers[register+1];
        int colorValue = (msbValue << 8) | lsbValue;
        return palToRGB(colorValue);
    }

    @Override
    public void next(final int cycles) {
        this.cycles = (this.cycles + cycles) % cyclesPerFrame;
        if (this.cycles > verticalBlankCycles) {
            if (registers[VBLANK] == 1) {
                framesSinceLastRead++;
                registers[FRAME] = (registers[FRAME] + 1) & 0xFF;
            }
            registers[VBLANK] = 0;
        } else {
            registers[VBLANK] = 1;
        }

        spi.update();
        if (spi.getSlaveSelect().value || spi.getPosition() < 8 || spi.getPosition() == lastPosition) {
            return;
        }
        if (spi.getPosition() == 8) {
            address = (spi.getToDeviceData() & 0x7F) << 8;
            readMode = (spi.getToDeviceData() & 0x80) == 0;
        } else if (spi.getPosition() == 16) {
            address |= spi.getToDeviceData();
            if (readMode) {
                if (address == FRAME) {
                    framesSinceLastRead = 0;
                }
                spi.setFromDeviceData(registers[address]);
                lastAddress = address;
                address++;
            }
        } else if (spi.getPosition() % 8 == 0) {
            if (readMode) {
                spi.setFromDeviceData(registers[address]);
            } else {
                if (address < 0x2800 || address > 0x2803) {
                    registers[address] = spi.getToDeviceData();
                }


                // TODO Only rebuild after SPI command is finished. Cache in another set?
                if (address >= RAM_PAL && (address < RAM_PAL + 0x0800)) {
                    ramChrToRebuild.add((address-RAM_PAL)/8);
                }
                if (address >= RAM_CHR && (address < RAM_CHR + 0x1000)) {
                    ramChrToRebuild.add((address-RAM_CHR)/16);
                }
                if (address >= RAM_SPR && (address < RAM_SPR + 0x0800) && ((address % 2) == 1)) {
                    spriteToRebuild.add(((address-RAM_SPR)/4) % 256); // TODO sprite pages
                }
                if (address >= RAM_SPRIMG && (address < RAM_SPRIMG + 0x4000)) {
                    spriteToRebuild.add(((address-RAM_SPRIMG)/256) % 256); // TODO sprite pages
                }
                if (address >= PALETTE_16A && (address < PALETTE_16A + 0x40)) {
                    spriteToRebuild.addAll(sprite16c);
                }
                if (address >= PALETTE_4A && (address < PALETTE_4A + 0x10)) {
                    spriteToRebuild.addAll(sprite4c);
                }
            }
            lastAddress = address;
            address++;
        }
        lastPosition = spi.getPosition();
    }

    @Override
    public void reset(final boolean hardReset) {
        if (hardReset) {
            readMode = false;
            lastPosition = 0;
            address = 0;
            cycles = 0;
            framesSinceLastRead = 0;
            System.arraycopy(memoryDump, 0, registers, 0, memoryDump.length);
            // RAMCHR
            for (int i = 0; i < 256; i++) {
                createRamChr(i);
                createSprite(i);
            }
            //spi.resetState();
        }
    }

    @Override
    public void draw(final Graphics graphics) {
        Graphics2D g2 = (Graphics2D)graphics;
        for (Iterator<Integer> it = ramChrToRebuild.iterator(); it.hasNext(); ) {
            Integer id = it.next();
            createRamChr(id);
            ramChrToRebuild.remove(id);
        }
        for (Iterator<Integer> it = spriteToRebuild.iterator(); it.hasNext(); ) {
            Integer id = it.next();
            createSprite(id);
            spriteToRebuild.remove(id);
        }
        g2.setFont(new Font("Courier", Font.PLAIN, 12));
        g2.setColor(Color.WHITE);
        g2.drawString(String.format("Frame: %02X - %04X: %02X", registers[0x2802],lastAddress, registers[lastAddress]),
            0, CANVAS_H+g2.getFontMetrics().getHeight());

        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(0,0,CANVAS_W+2,CANVAS_H+2);
        g2.translate(1,1);
        g2.clipRect(0,0,CANVAS_W,CANVAS_H);
        g2.setBackground(new Color(getBackgroundColor()));
        g2.clearRect(0,0, CANVAS_W,CANVAS_H);
        int scrollX = registers[0x2804] | (registers[0x2805] << 8);
        int scrollXPixels = (scrollX & 7) * 2;
        int scrollY = registers[0x2806] | (registers[0x2807] << 8);
        int scrollYPixels = (scrollY & 7) * 2;
        for (int y = 0; y < 38; y++) {
            for (int x = 0; x < 51; x++) {
                int cx = (x + (scrollX >> 3)) & 0x3f;
                int cy = (y + (scrollY >> 3)) & 0x3f;
                int charCode = registers[cy*64+cx];
                AffineTransform transform =
                    ramChr[charCode].createGraphics().getTransform();
                transform.translate(x*16 - scrollXPixels,y*16 - scrollYPixels);
                transform.scale(2,2);
                g2.drawImage(ramChr[charCode], transform, null);
            }
        }
        for (int i = 0; i < 256; i++) {
            AffineTransform transform = spriteImages[i].createGraphics().getTransform();
            long spriteData = getSpriteData(i);
            int x = (int)(spriteData & 511);
            int y = (int)((spriteData >> 16) & 511);
            transform.translate(x*2, y*2);
            int rotationBits = (registers[RAM_SPR + i * 4 + 1] >> 1) & 7;
            transform.rotate(spriteRotations[rotationBits], 16,16);
            transform.scale(spriteXTransforms[rotationBits],spriteYTransforms[rotationBits]);
            g2.drawImage(spriteImages[i], transform, null);
        }
    }

}
