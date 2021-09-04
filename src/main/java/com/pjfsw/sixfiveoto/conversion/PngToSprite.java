package com.pjfsw.sixfiveoto.conversion;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class PngToSprite {
    public static void main(String args[]) {
        String name = args[0];
        String uname = name.toUpperCase();
        int w = Integer.parseInt(args[1]);
        int h = Integer.parseInt(args[2]);
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
            List<String> hFile = new ArrayList<>();
            hFile.add(String.format("#ifndef _%s_H", uname));
            hFile.add(String.format("#define _%s_H", uname));
            hFile.add(String.format("// Size %d x %d", w,h));
            hFile.add(String.format("const uint8_t %s_width = %d;", name, w));
            hFile.add(String.format("const uint8_t %s_height = %d;", name, h));
            hFile.add(String.format("uint8_t %s[] = {", name));
            String hLine = "";
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    if (hLine.isEmpty()) {
                        hLine = "    ";
                    }
                    int rgba = img.getRGB(x,y);
                    int b = rgba & 255;
                    int g = (rgba >> 8) & 255;
                    int r = (rgba >> 16) & 255;
                    int a = (rgba >> 24) & 255;
                    if (a > 0) {
                        int rgb = (b >> 6) | ((g >> 6) << 2) | ((r >> 6) << 4);
                        hLine += String.format("0x%02x", rgb);
                    } else {
                        hLine += "0x80";
                    }
                    hLine += ", ";
                    if (hLine.length() > 120) {
                        hFile.add(hLine);
                        hLine = "";
                    }
                }
            }
            if (!hLine.isEmpty()) {
                hFile.add(hLine);
            }
            hFile.add("};");
            hFile.add("#endif");
            Files.write(new File(name+".h").toPath(), hFile, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
