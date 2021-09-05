package com.pjfsw.sixfiveoto.conversion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        consumers.add(new JifFileConsumer(name));

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

    private Map<Integer,Integer> getColorFrequency(BufferedImage img) {
        Map<Integer,Integer> freq = new HashMap<>() ;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = getRgb6(img.getRGB(x + xofs, y + yofs));
                if (freq.containsKey(rgb)) {
                    freq.put(rgb, freq.get(rgb)+1);
                } else {
                    freq.put(rgb, 1);
                }
            }
        }
        return freq;
    }

    private static int getRgb6(int rgba) {
        rgba = rgba & 0xFFFFFF;
        int b = (rgba & 255) >> 6;
        int g = ((rgba >> 8) & 255) >> 6;
        int r = ((rgba >> 16) & 255) >> 6;
        return b | (g << 2) | (r << 4);
    }

    private static int findNearestColor(int c, Set<Integer> colors) {
        int minDistance = Integer.MAX_VALUE;
        int nearestColor = 0;
        int r1 = (c >> 4) & 3;
        int g1 = (c >> 2) & 3;
        int b1 = c & 3;
        for (Integer color : colors) {
            int r2 = (color >> 4) & 3;
            int g2 = (color >> 2) & 3;
            int b2 = color & 3;
            int r = r1-r2;
            int g = g1-g2;
            int b = b1-b2;
            int distance = r*r + g*g + b*b;
            if (distance < minDistance) {
                nearestColor = color;
                minDistance = distance;
            }
        }
        return nearestColor;
    }

    public void run() throws IOException {
        Map<Integer,Integer> colors = new LinkedHashMap<>();
        Map<Integer,Integer> colorFrequency = getColorFrequency(img);
        List<Integer> rgbList = new ArrayList<>(colorFrequency.keySet());
        rgbList.sort(Comparator.comparingInt(key -> -colorFrequency.get(key)));
        // Swap darkest color to be 0
        int darkest = Integer.MAX_VALUE;
        int index = 0;
        for (int i = 0; i < rgbList.size(); i++) {
            if (rgbList.get(i) < darkest) {
                index = i;
                darkest = rgbList.get(i);
            }
        }
        if (index > 0) {
            int tmp = rgbList.get(0);
            rgbList.set(0, rgbList.get(index));
            rgbList.set(index, tmp);
        }

        for (int i = 0; i < 16; i++) {
            colors.put(rgbList.get(i), i);
        }

        colorFrequency.forEach((k,v) -> System.out.printf("Color %d = %d%n", k, v));
        rgbList.forEach(rgb -> System.out.printf("Palette = %d%n", rgb));
        for (int y = 0; y < h; y++) {
            int shift = 16;
            int bitmap = 0;
            for (int x = 0; x < w; x++) {
                if (shift == 0) {
                    bitmap = 0;
                    shift = 16;
                }
                shift -= 4;
                int rgba = img.getRGB(x+xofs,y+yofs);
                int c = getRgb6(rgba);
                if (!colors.containsKey(c)) {
                    c = findNearestColor(c, colors.keySet());
                }
                bitmap = bitmap | (colors.get(c) << shift);
                if (shift == 0) {
                    for (OutputConsumer consumer : consumers) {
                        consumer.consumePixels(bitmap >> 8);
                        consumer.consumePixels(bitmap & 255);
                    }
                }
            }
        }
        Integer[] paletteRgb = colors.keySet().toArray(new Integer[0]);
        for (OutputConsumer consumer : consumers) {
            consumer.consumeWidthInBytes(w/2);
            consumer.consumeHeight(h);
            consumer.consumePalette(paletteRgb);
            consumer.produce();
        }
    }
}
