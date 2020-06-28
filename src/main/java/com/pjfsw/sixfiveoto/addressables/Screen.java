package com.pjfsw.sixfiveoto.addressables;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Screen implements Poker, Peeker {
    private final PixelFrame pixelComponent;
    private final JFrame frame;
    private boolean verticalBlank;

    public Screen() {
        frame = new JFrame();
        frame.setPreferredSize(new Dimension(256,256));
        pixelComponent = new PixelFrame(16);
        frame.add(pixelComponent);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    @Override
    public void poke(final int address, final int data) {
        pixelComponent.poke(address,data);
    }

    public void draw() {
        verticalBlank = true;
        frame.getContentPane().repaint();
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

    private static class PixelFrame extends JComponent {
        private final int w = 16;
        private final int h = 16;
        private final BufferedImage img;
        private static final int[] redgreen = {0,36,73,109,146,182,219,255};
        private static final int[] blue = {0,85,170,255};

        private PixelFrame(int scale) {
            this.img = new BufferedImage(w,h, TYPE_INT_RGB);
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

        @Override
        public void paintComponent(Graphics g) {
            ((Graphics2D)g).drawImage(img, 0,0,  256 ,256, null);
        }
    }
}
