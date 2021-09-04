package com.pjfsw.sixfiveoto.conversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;

public class BinFileConsumer extends OutputConsumerAdapter {
    private final String name;
    private List<Byte> pixelData = new ArrayList<>();

    public BinFileConsumer(String name) {
        this.name = name;

    }

    @Override
    public void consumePixels(int left, int right) {
        pixelData.add((byte)(left));
        pixelData.add((byte)(right));
    }

    @Override
    public void produce() throws IOException {
        byte[] buffer = new byte[pixelData.size()];
        int n = 0;
        for (Byte b : pixelData) {
            buffer[n++] = b;
        }
        Files.write(buffer, new File(name+"_pixels.bin"));
        Integer[] palette = getPalette();
        if (palette == null) {
            return;
        }
        buffer = new byte[palette.length];
        n = 0;
        for (Integer integer : palette) {
            buffer[n++] = (byte)(integer & 0xFF);
        }
        Files.write(buffer, new File(name+"_palette.bin"));
    }
}
