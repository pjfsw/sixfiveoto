package com.pjfsw.sixfiveoto.conversion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class ImageToBitmap {
    private final ArrayList<OutputConsumer> consumers;
    private final BufferedImage img;
    private final int w;
    private final int h;
    private final int xofs;
    private final int yofs;

    public static void main(String[] args) {
        try {
            new ImageToBitmap(args).run();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    public ImageToBitmap(String[] args) throws IOException {
        String name = args[0];
        w = Integer.parseInt(args[1]);
        h = Integer.parseInt(args[2]);
        if (args.length > 4) {
            xofs = Integer.parseInt(args[3]);
            yofs = Integer.parseInt(args[4]);
        } else {
            xofs = 0;
            yofs = 0;
        }
        consumers = new ArrayList<>();
        consumers.add(new HeaderFileConsumer(name));
        consumers.add(new BinFileConsumer(name));

        File jpg = new File(args[0]+".jpg");
        File png = new File(args[0]+".png");
        File f;
        if (jpg.exists()) {
            f = jpg;
        } else {
            f = png;
        }
        img = ImageIO.read(f);
    }

    public void run() throws IOException {
        Map<Integer,Integer> colors = new HashMap<>();
        for (int y = 0; y < h; y++) {
            int shift = 16;
            int bitmap = 0;
            for (int x = 0; x < w; x++) {
                if (shift == 0) {
                    bitmap = 0;
                    shift = 16;
                }
                shift -= 2;
                int rgba = img.getRGB(x+xofs,y+yofs);
                int b = (rgba & 255) >> 6;
                int g = ((rgba >> 8) & 255) >> 6;
                int r = ((rgba >> 16) & 255) >> 6;
                int c = b | (g << 2) | (r << 4);
                int a = (rgba >> 24) & 255;
                if (a > 0) {
                    if (!colors.containsKey(c)) {
                        colors.put(c, colors.size());
                    }
                    bitmap = bitmap | (colors.get(c) << shift);
                }
                if (shift == 0) {
                    for (OutputConsumer consumer : consumers) {
                        // Now left is in highbyte and right is in lowbyte. Let receiver handle
                        // endianness
                        consumer.consumePixels(bitmap >> 8, bitmap & 255);
                    }
                }
            }
            if (colors.size() > 4) {
                System.err.printf("Warning, too many (%d) colours%n", colors.size());
            }
        }
        Integer[] paletteRgb = colors.keySet().toArray(new Integer[0]);
        for (OutputConsumer consumer : consumers) {
            consumer.consumeWidth(w);
            consumer.consumeHeight(h);
            consumer.consumePalette(paletteRgb);
            consumer.produce();
        }
    }
}
