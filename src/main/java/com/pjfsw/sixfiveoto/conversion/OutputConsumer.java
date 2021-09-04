package com.pjfsw.sixfiveoto.conversion;

import java.io.IOException;

public interface OutputConsumer {
    void consumeWidthInBytes(int width);
    void consumeHeight(int height);
    void consumePalette(Integer[] palette);
    void consumePixels(int aByte);
    void produce() throws IOException;
}
