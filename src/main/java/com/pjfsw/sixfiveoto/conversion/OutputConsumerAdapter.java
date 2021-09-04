package com.pjfsw.sixfiveoto.conversion;

abstract public class OutputConsumerAdapter implements OutputConsumer {
    private int width;
    private int height;
    private Integer[] palette;

    @Override
    public void consumeWidthInBytes(int width) {
        this.width = width;
    }

    @Override
    public void consumeHeight(int height) {
        this.height = height;
    }

    @Override
    public void consumePalette(Integer[] palette) {
        this.palette = palette;

    }

    public int getWidthInBytes() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Integer[] getPalette() {
        return palette;
    }
}
