package com.pjfsw.sixfiveoto.conversion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class PngToSprite {
    public static void main(String args[]) {
        String name = args[0];
        int w = Integer.parseInt(args[1]);
        int h = Integer.parseInt(args[2]);
        List<OutputConsumer> consumers = new ArrayList<>();
        consumers.add(new BinFileConsumer(name));
        consumers.add(new HeaderFileConsumer(name));

        try {
            File jpg = new File(args[0]+".jpg");
            File png = new File(args[0]+".png");
            File f;
            if (jpg.exists()) {
                f = jpg;
            } else {
                f = png;
            }
            BufferedImage img = ImageIO.read(f);
            for (int y = 0; y < h; y++) {
                int left = 0;
                for (int x = 0; x < w; x++) {
                    int rgba = img.getRGB(x,y);
                    int b = rgba & 255;
                    int g = (rgba >> 8) & 255;
                    int r = (rgba >> 16) & 255;
                    int a = (rgba >> 24) & 255;
                    int rgb = 0x80;
                    if (a > 0) {
                        rgb = (b >> 6) | ((g >> 6) << 2) | ((r >> 6) << 4);
                    }
                    for (OutputConsumer consumer : consumers) {
                        consumer.consumePixels(rgb);
                    }
                }
            }
            for (OutputConsumer consumer : consumers) {
                consumer.consumeWidthInBytes(w);
                consumer.consumeHeight(h);
                consumer.produce();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
