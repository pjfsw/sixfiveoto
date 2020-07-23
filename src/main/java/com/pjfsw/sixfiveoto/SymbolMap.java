package com.pjfsw.sixfiveoto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SymbolMap {
    public static final Pattern SYMBOL_RE = Pattern.compile(".label\\s*(\\w+)=\\s*(\\S+)\\s?.*");

    public static Map<Integer,String> getSymbolsFromPrg(String prgName) {
        File symbolFile = new File(prgName.replace(".prg", ".sym"));
        Map<Integer, String> symbolMap = new HashMap<>();
        if (symbolFile.isFile()) {
            try {
                System.out.println("- Loading detected symbol file " + symbolFile.getName());
                List<String> symbols =
                    Files.readAllLines(symbolFile.toPath());
                for (String symbol: symbols) {
                    Matcher m = SYMBOL_RE.matcher(symbol);
                    if (m.matches()) {

                        String s = m.group(2).substring(1);
                        int address = Integer.parseInt(s, 16);
                        symbolMap.put(address, m.group(1));
                    }
                }
            } catch (IOException e) {
                System.out.println(String.format("- Failed to load symbol file %s: %s",
                    symbolFile.getName(), e.getMessage()));
            }
        }
        return symbolMap;
    }
}
