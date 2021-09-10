package com.pjfsw.sixfiveoto;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

public class SourceLoader {
    public static String compileSource(String source) throws InterruptedException, IOException {
        String assembler = System.getProperty("assembler");
        if (assembler == null) {
            System.out.println("Cannot assemble on the fly as 'assembler' property is not defined!");
            System.out.println("Use Java property -Dassembler=\"java -jar /path/to/KickAssembler/KickAss.jar\"");
            System.out.println("Send a .PRG file as argument to launch emulator directly");
            System.exit(1);
        }

        System.out.printf("[Assembling \"%s\"%n", source);
        Process ps = Runtime.getRuntime().exec(ArrayUtils.addAll(assembler.split(" "), source));
        int result = ps.waitFor();
        InputStream is = ps.getInputStream();
        StringWriter writer = new StringWriter();
        String output = IOUtils.toString(is, StandardCharsets.UTF_8);
        System.out.println(output);
        if (result == 0) {
            return source.replace(".asm", ".prg");
        }
        System.err.printf("Result code when launching assembler: %d%n", result);
        return null;
    }

}
