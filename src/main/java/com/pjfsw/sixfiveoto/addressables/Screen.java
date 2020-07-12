package com.pjfsw.sixfiveoto.addressables;

import static java.awt.image.BufferedImage.TYPE_INT_RGB;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Screen implements Poker, Peeker {
    public static final int W = 816;
    public static final int H = 768;
    //private final PixelFrame pixelComponent;
    private final JFrame frame;
    private final BufferStrategy strategy;
    private int frameCounter;
    private volatile boolean running = true;
    private final List<PositionedDrawable> drawables = new ArrayList<>();

    public Screen() {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsConfiguration gc = gs[0].getConfigurations()[0];

        frame = new JFrame(gc);
        frame.setPreferredSize(new Dimension(W,H));
        int pixelSize = 256;
        /*pixelComponent = new PixelFrame(pixelSize,pixelSize);
        addDrawable(new Point(0,0), pixelComponent);*/
        frame.pack();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.createBufferStrategy(2);
        strategy = frame.getBufferStrategy();
    }

    public void addDrawable(Point position, Drawable drawable) {
        drawables.add(new PositionedDrawable(position, drawable));
    }

    public void interrupt() {
        this.running = false;
    }

    public void increaseFrameCounter() {
        frameCounter++;
    }

    private static void waitNs(long ns) {
        if (ns > 0) {
            try {
                Thread.sleep(ns / 1000000, (int)(ns % 1000000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void loop() {
        long ticks = System.nanoTime();
        while (running) {
            do {
                // preparation for rendering ?
                do {
                    Graphics graphics = strategy.getDrawGraphics();
                    graphics.setColor(Color.BLACK);
                    graphics.fillRect(0,0,W,H);
                    graphics.translate(0, frame.getInsets().top);
                    draw(graphics);
                    graphics.dispose();
                } while (strategy.contentsRestored());
                strategy.show();
                long wait = 16_000_000 - (int)(System.nanoTime() - ticks);
                ticks = System.nanoTime();
                waitNs(wait);
            } while (strategy.contentsLost());
        }
        frame.setVisible(false);
        frame.dispose();

    }

    @Override
    public void poke(final int address, final int data) {
//        pixelComponent.poke(address,data);
    }

    public void draw(Graphics graphics) {
        for (PositionedDrawable pd : drawables) {
            Graphics g = graphics.create();
            g.translate(pd.position.x, pd.position.y);
            pd.drawable.draw(g);
        }
    }

    @Override
    public int peek(final int address) {
        int oldFrameCounter = frameCounter;
        frameCounter = 0;
        return oldFrameCounter;
    }


    private static class PixelFrame implements Drawable {
        private final int w = 32;
        private final int h = 32;
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
            g2.translate(1, 1);
            g2.drawImage(img, 0,0, targetWidth, targetHeight, null);
        }
    }

    private static class PositionedDrawable {
        private final Point position;
        private final Drawable drawable;

        private PositionedDrawable(Point position, Drawable drawable) {
            this.position = position;
            this.drawable = drawable;
        }

    }
}
