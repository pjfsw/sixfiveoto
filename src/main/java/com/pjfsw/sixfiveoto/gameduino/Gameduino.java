package com.pjfsw.sixfiveoto.gameduino;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.spi.Spi;

/**
 * 0x0000-0x0FFF RAM_PIC
 * 0x1000-0x1FFF RAM_CHR
 *    4 pixels per byte 11223344
 * 0x2000-0x27FF RAM_PAL
 *
 */
public class Gameduino implements Drawable, Clockable {
    public static final int W = 800;
    public static final int H = 600;

    private static final int RAM_CHR = 0x1000;
    private static final int RAM_PAL = 0x2000;
    private static final int RAM_SPR = 0x3000;
    private static final int RAM_SPRPAL = 0x3800;
    private static final int RAM_SPRIMG = 0x4000;

    private final Spi spi;
    private boolean readMode;
    private int address;
    private final int[] registers = new int[32768];
    private int lastPosition;

    private final BufferedImage[] ramChr = new BufferedImage[256];
    private final BufferedImage[] spriteImages = new BufferedImage[256];

    private final int[] fiveBitColors = new int[32];

    private final Set<Integer> ramChrToRebuild = ConcurrentHashMap.newKeySet();


    public Gameduino(Spi spi, int[] memoryDump) {
        this.spi = spi;

        System.arraycopy(memoryDump, 0, registers, 0, memoryDump.length);
        for (int i = 0; i < 32; i++) {
            fiveBitColors[i] = 255 * i / 31;
        }
        // RAMCHR
        for (int i = 0; i < 256; i++) {
            createRamChr(i);
            createSprite(i);
        }
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

        int palette;
        if ((palette = get8BitPalette(spriteData)) != -1) {
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int colorIndex = registers[RAM_SPRIMG + sourceImage  * 256 + y * 16 + x];
                    spriteImages[i].setRGB(x,y, getColorRegisterValue(RAM_SPRPAL + palette * 512 + colorIndex*2));
                    //spriteImages[i].setRGB(x,y, 0xFFFFFFFF);
                }
            }
        } else if ((palette = get4BitPalette(spriteData)) != -1) {
            int nibble = (int)((spriteData >> 13) & 1);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int colorByte = registers[RAM_SPRIMG + sourceImage * 256 + y * 16 + x];
                    int colorIndex = (colorByte >> (4*nibble)) & 15;
                    spriteImages[i].setRGB(x,y, getColorRegisterValue(RAM_SPRPAL + palette * 32 + colorIndex*2));

                    //spriteImages[i].setRGB(x,y, 0xFF333333);
                }
            }
        } else {
            palette = get2BitPalette(spriteData);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    spriteImages[i].setRGB(x,y, 0xFFFF0000);
                }
            }
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
                spi.setFromDeviceData(registers[address]);
                address++;
            }
        } else if (spi.getPosition() % 8 == 0) {
            if (readMode) {
                spi.setFromDeviceData(registers[address]);
            } else {
                registers[address] = spi.getToDeviceData();
                if (address >= RAM_PAL & address < RAM_PAL + 2048) {
                    ramChrToRebuild.add((address-RAM_PAL)/8);
                }
                if (address >= RAM_CHR & address < RAM_CHR + 4096) {
                    ramChrToRebuild.add((address-RAM_CHR)/16);
                }
            }
            address++;
        }
        lastPosition = spi.getPosition();
    }

    @Override
    public void draw(final Graphics graphics) {
        Graphics2D g2 = (Graphics2D)graphics;
        for (Iterator<Integer> it = ramChrToRebuild.iterator(); it.hasNext(); ) {
            Integer id = it.next();
            createRamChr(id);
            ramChrToRebuild.remove(id);
        }
        g2.setBackground(new Color(getBackgroundColor()));
        g2.clearRect(0,0, 800,600);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(0,0,W,H);
        for (int y = 0; y < 37; y++) {
            for (int x = 0; x < 50; x++) {
                int charCode = registers[y*64+x];
                AffineTransform transform =
                    ramChr[charCode].createGraphics().getTransform();
                transform.translate(x*16,y*16);
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
            transform.scale(2,2);
            g2.drawImage(spriteImages[i], transform, null);
        }
    }
}
