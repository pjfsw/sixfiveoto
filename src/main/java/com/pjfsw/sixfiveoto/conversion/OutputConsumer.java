package com.pjfsw.sixfiveoto.conversion;

import java.io.IOException;

public interface OutputConsumer {
    void consumeWidth(int width);
    void consumeHeight(int height);
    void consumePalette(Integer[] palette);
    void consumePixels(int left, int right);
    void produce() throws IOException;
}
