package com.pjfsw.sixfiveoto.picogfx;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Byte 0 : Letter 'J'
 * Byte 1 : Width in bytes
 * Byte 2-3 : Height in pixels
 * Byte 4 : Length of palette n
 * Byte 5..n : Palette
 * Byte n+1.. : pixel data
 */
public class Jif {
    private final int[] data;
    private final int width;
    private final int height;
    private final int[] palette;

    public Jif(int[] data, int width, int height, int[] palette) {
        this.data = data;
        this.width = width;
        this.height = height;
        this.palette = palette;
    }

    public static Jif load(String filename) throws URISyntaxException, IOException {
        Path path = new File(PicoGfx.class.getResource(filename).toURI()).toPath();
        byte[] bytes = Files.readAllBytes(path);
        if (bytes[0] != 0x4a) { // 'J'
            throw new IllegalArgumentException(String.format("%s is not a valid JIF file!", filename));
        }
        int[] betterBytes = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            betterBytes[i] = ((int)bytes[i]) & 0xFF;
        }

        int width = betterBytes[1];
        int height = betterBytes[2] + (betterBytes[3] << 8);
        int paletteLength = betterBytes[4];

        int notPixelBytes = 5 + paletteLength;
        int expectedLength = (notPixelBytes + width * height);
        if (betterBytes.length != expectedLength) {
            throw new IllegalArgumentException(String.format("File %s is %d bytes (expected %d bytes)",
                filename, betterBytes.length, expectedLength));
        }
        int[] palette = new int[paletteLength];
        System.arraycopy(betterBytes, 5, palette, 0, paletteLength);
        int[] data = new int[betterBytes.length-notPixelBytes];
        System.arraycopy(betterBytes, notPixelBytes, data, 0, betterBytes.length-notPixelBytes);
        return new Jif(data, width, height, palette);
    }

    public int[] getData() {
        return data;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getPalette() {
        return palette;
    }
}

