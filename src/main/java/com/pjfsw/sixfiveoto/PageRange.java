package com.pjfsw.sixfiveoto;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

public final class PageRange {
    private final int start;
    private final int end;

    public PageRange(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("Start is higher than end");
        }
        this.start = start;
        this.end = end;
    }

    public static PageRange createFromProperty(Config properties, String partName) {
        String map = properties.getProperty(partName+".map");

        if (map == null) {
            return null;
        }

        List<Integer> pages = Stream.of(map.split(","))
            .map(String::trim)
            .map(page -> Integer.parseInt(page, 16))
            .collect(toList());
        if (pages.size() != 2) {
            throw new IllegalArgumentException(
                String.format("Part name %s: Must specify start and end page i.e. F0,FF", partName));
        }

        return new PageRange(pages.get(0), pages.get(1));
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
