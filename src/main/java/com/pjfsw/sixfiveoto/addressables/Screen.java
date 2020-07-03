package com.pjfsw.sixfiveoto.addressables;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Screen implements Poker, Peeker {
    private final PixelFrame pixelComponent;
    private final JFrame frame;
    private final BufferStrategy strategy;
    private boolean verticalBlank;
    private volatile boolean running = true;

    public Screen(int w, int h) {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsConfiguration gc = gs[0].getConfigurations()[0];

        frame = new JFrame(gc);
        frame.setPreferredSize(new Dimension(w,h));
        pixelComponent = new PixelFrame(w, h);
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.createBufferStrategy(2);
        strategy = frame.getBufferStrategy();
    }

    public void interrupt() {
        this.running = false;
    }

    public void loop() {
        long ticks = System.nanoTime();
        while (running) {
            do {
                // preparation for rendering ?
                do {
                    Graphics graphics = strategy.getDrawGraphics();
                    draw(graphics);
                    graphics.dispose();
                } while (strategy.contentsRestored());
                strategy.show();
                long wait = 16666666 - (int)(System.nanoTime() - ticks);
                ticks = System.nanoTime();
                if (wait > 0) {
                    try {
                        Thread.sleep(wait / 1000000, (int)(wait % 1000000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (strategy.contentsLost());
        }
        //w.setVisible(false);
        //w.dispose();

    }

    @Override
    public void poke(final int address, final int data) {
        pixelComponent.poke(address,data);
    }

    public void draw(Graphics graphics) {
        verticalBlank = true;
        pixelComponent.draw(graphics);
    }

    @Override
    public int peek(final int address) {
        if (verticalBlank) {
            verticalBlank = false;
            return 1;
        } else {
            return 0;
        }
    }

    private static class PixelFrame {
        private final int w = 16;
        private final int h = 16;
        private final BufferedImage img;
        private static final int[] redgreen = {0,36,73,109,146,182,219,255};
        private static final int[] blue = {0,85,170,255};
        private final int targetWidth;
        private final int targetHeight;

        private PixelFrame(int targetWidth, int targetHeight) {
            this.img = new BufferedImage(w,h, TYPE_INT_RGB);
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    this.img.setRGB(x,y,0);
                }
            }
        }
        public void poke(final int address, final int data) {
            int pos = address % (w * h);
            int x = pos % w;
            int y = pos / w;
            int r = data >> 5;
            int g = (data >> 2) & 7;
            int b = data & 3;
            img.setRGB(x,y, (redgreen[r] << 16) | (redgreen[g] << 8) | blue[b] );
        }

        public void draw(Graphics g) {
            Graphics2D g2 = ((Graphics2D)g);
            Rectangle bounds = g2.getDeviceConfiguration().getBounds();
            g2.drawImage(img, 0,0, targetWidth, targetHeight, null);
        }
    }
}
