package com.pjfsw.sixfiveoto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class Config {
    private final Map<String,String> properties = new LinkedHashMap<>();

    public static Config createFromFile(String filename) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filename)).stream()
            .map(line -> (line.indexOf('#') > - 1) ? line.substring(0, line.indexOf('#')) : line)
            .filter(line -> !line.trim().isEmpty())
            .collect(Collectors.toList());

        Config config = new Config();
        for (String line : lines) {
            String[] keyValue = line.split("=");
            if (keyValue.length != 2 || keyValue[0].trim().isEmpty() || keyValue[1].trim().isEmpty()) {
                System.out.println("Ignoring malformed line: "+line);
                continue;
            }
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();
            config.properties.put(key,value);
        }
        return config;
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getOrDefault(key, defaultValue);
    }

    public String getProperty(String key) {
        return properties.getOrDefault(key, null);
    }


    public Collection<String> stringPropertyNames() {
        return properties.keySet();
    }
}
