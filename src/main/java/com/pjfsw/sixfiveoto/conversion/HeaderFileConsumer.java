package com.pjfsw.sixfiveoto.conversion;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class HeaderFileConsumer extends OutputConsumerAdapter {
    private final String name;
    private final List<String> hFile;
    private String hLine;

    public HeaderFileConsumer(String name) {
        this.name = name;
        String uname = name.toUpperCase();
        hFile = new ArrayList<>();
        hFile.add(String.format("#ifndef _%s_H", uname));
        hFile.add(String.format("#define _%s_H", uname));
        hFile.add(String.format("uint8_t %s[] = {", name));
        hLine = "";
    }


    @Override
    public void consumePixels(int aByte) {
        if (hLine.isEmpty()) {
            hLine = "    ";
        }
        hLine += String.format("0x%02x, ", aByte);
        if (hLine.length() > 120) {
            hFile.add(hLine);
            hLine = "";
        }
    }

    @Override
    public void produce() throws IOException {
        if (!hLine.isEmpty()) {
            hFile.add(hLine);
        }
        hFile.add("};");
        hFile.add(String.format("const uint16_t %s_width = %d;", name, getWidthInBytes()));
        hFile.add(String.format("const uint16_t %s_height = %d;", name, getHeight()));
        Integer[] palette = getPalette();
        if (palette != null) {
            hFile.add(String.format("uint8_t %s_palette[] = {", name));
            String s = "    ";
            for (Integer col : palette) {
                s += String.format("0x%02x, ", col);
            }
            hFile.add(s);
            hFile.add("};");
        }
        hFile.add("#endif");
        Files.write(new File(name+".h").toPath(), hFile, Charset.defaultCharset());

    }
}
