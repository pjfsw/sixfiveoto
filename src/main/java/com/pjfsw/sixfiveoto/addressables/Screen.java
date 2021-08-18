package com.pjfsw.sixfiveoto.addressables;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

public class Screen implements Peeker {
    private static final int WAIT_PERIOD = 1_000 / 60;
    public static final int W = 808;
    public static final int H = 768;
    //private final PixelFrame pixelComponent;
    private final JFrame frame;
    private int frameCounter;
    private volatile boolean running = true;
    private int fps = 0;
    private final List<PositionedDrawable> drawables = new ArrayList<>();

    public Screen() {

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsConfiguration gc = gs[0].getConfigurations()[0];

        frame = new JFrame("A Fine Emulator of 65C02", gc);
        frame.setPreferredSize(new Dimension(W,H));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.createBufferStrategy(2);
        frame.setAlwaysOnTop(true);
        frame.setAlwaysOnTop(false);
    }

    public int getScreenHeight() {
        return H - frame.getInsets().bottom - frame.getInsets().top;
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

    private static void waitMs(long millis) {
        if (millis > 0) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void loop() {
        BufferStrategy strategy = frame.getBufferStrategy();
        long lastTime = System.currentTimeMillis();
        long frameTime = 0;
        long frames = 0;
        while (running) {
            long now = System.currentTimeMillis();
            long deltaTime = now - lastTime;
            lastTime = now;
            frameTime += deltaTime;
            if (frameTime > 1000) {
                fps = (int)(1000 * frames/frameTime);
                frames = 0;
                frameTime -= 1000;
            }
            Graphics graphics = strategy.getDrawGraphics();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0,0,W,H);
            graphics.translate(0, frame.getInsets().top);
            draw(graphics);
            graphics.dispose();
            frames++;
            strategy.show();
            long wait = WAIT_PERIOD - (int)(System.currentTimeMillis() - lastTime);
            if (wait > 1) {
                waitMs(wait);
            }
        }
        frame.setVisible(false);
        frame.dispose();

    }

    public void draw(Graphics graphics) {
        for (PositionedDrawable pd : drawables) {
            Graphics g = graphics.create();
            g.translate(pd.position.x, pd.position.y);
            pd.drawable.draw(g);
        }
        graphics.setColor(Color.GRAY);
        graphics.drawString(String.format("%d", fps), 0 ,610);
    }

    @Override
    public int peek(final int address) {
        int oldFrameCounter = frameCounter;
        frameCounter = 0;
        return oldFrameCounter;
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
