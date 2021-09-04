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
        hFile.add(String.format("uint16_t %s[] = {", name));
        hLine = "";
    }


    @Override
    public void consumePixels(int left, int right) {
        if (hLine.isEmpty()) {
            hLine = "    ";
        }
        // This weird swapping is due to little endian
        hLine += String.format("0x%04x, ", (right << 8) | left);
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
        hFile.add(String.format("const uint16_t %s_width = %d;", name, getWidth()));
        hFile.add(String.format("const uint16_t %s_height = %d;", name, getHeight()));
        Integer[] palette = getPalette();
        if (palette  != null) {
            String paletteString = String.format("uint32_t %s_palette = 0x%02x%02x%02x%02x;",
                name,
                palette[3],
                palette[2],
                palette[1],
                palette[0]
            );
            hFile.add(paletteString);
        }
        hFile.add("#endif");
        Files.write(new File(name+".h").toPath(), hFile, Charset.defaultCharset());

    }
}
