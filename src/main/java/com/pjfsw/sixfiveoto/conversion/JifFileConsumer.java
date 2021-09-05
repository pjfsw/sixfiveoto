package com.pjfsw.sixfiveoto.conversion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.io.Files;

public class JifFileConsumer extends OutputConsumerAdapter {
    private final String name;
    private List<Byte> pixelData = new ArrayList<>();

    public JifFileConsumer(String name) {
        this.name = name;

    }

    @Override
    public void consumePixels(int aByte) {
        pixelData.add((byte)(aByte));
    }

    @Override
    public void produce() throws IOException {
        Integer[] palette = getPalette();
        if (palette == null) {
            palette = new Integer[0];
        }
        // Store "J", width in bytes (8-bit), height (16-bit little endian), pixel data, 4 byte palette
        byte[] buffer = new byte[5+palette.length + pixelData.size()];
        buffer[0] = (byte)'J';
        buffer[1] = (byte)(getWidthInBytes());
        buffer[2] = (byte)(getHeight()&0xff);
        buffer[3] = (byte)(getHeight()>>8);
        buffer[4] = (byte)palette.length;
        int n = 5;
        for (Integer color : palette) {
            buffer[n++] = (byte)(color & 0xff);
        }
        for (Byte b : pixelData) {
            buffer[n++] = b;
        }
        Files.write(buffer, new File(name+".jif"));
    }
}
