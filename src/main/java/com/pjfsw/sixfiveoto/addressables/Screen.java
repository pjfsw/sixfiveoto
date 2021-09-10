package com.pjfsw.sixfiveoto.addressables;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.pjfsw.sixfiveoto.CpuStatistics;

public class Screen {
    private static final int WAIT_PERIOD = 1_000 / 60;
    public static final int W = 800;
    public static final int H = 744;
    //private final PixelFrame pixelComponent;
    private final JFrame frame;
    private final CpuStatistics cpuStatistics;
    private final Font font;
    private final int leftOffset;
    private final int topOffset;
    private final float scaling;
    private volatile boolean running = true;
    private int fps = 0;
    private final List<PositionedDrawable> drawables = new ArrayList<>();

    public Screen(CpuStatistics cpuStatistics, float scaling) {
        this.cpuStatistics = cpuStatistics;
        this.scaling = scaling;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsConfiguration gc = gs[0].getConfigurations()[0];
        this.font = new Font("Courier", Font.PLAIN, 14);

        frame = new JFrame("A Fine Emulator of 65C02", gc);

        frame.setPreferredSize(new Dimension(W ,H));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        Insets insets = frame.getInsets();
        leftOffset = insets.left;
        topOffset = insets.top;
        int bottomOffset = insets.bottom;
        int rightOffset = insets.right;
        frame.setPreferredSize(new Dimension(
            (int)(scaling*W)+leftOffset + rightOffset,
            (int)(scaling*H)+topOffset + bottomOffset)
        );
        frame.pack();
        frame.setLocationRelativeTo(null);

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
            Graphics2D g2 = (Graphics2D)graphics;
            g2.setColor(Color.BLACK);
            g2.translate(leftOffset, topOffset);
            g2.scale(scaling, scaling);
            g2.fillRect(0,0,W,H);
            draw(g2);
            graphics.dispose();
            frames++;
            strategy.show();
            long wait = WAIT_PERIOD - (int)(System.currentTimeMillis() - lastTime);
            if (wait > 0) {
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
        Color normalColor = Color.GRAY;
        Color angryColor = Color.RED;
        graphics.setFont(font);
        graphics.setColor(normalColor);
        graphics.drawString(String.format("CPU: %.2f MHz", cpuStatistics.getSpeed()), 2, 610);
        long irqUsage = cpuStatistics.irqUsage();
        if (irqUsage > 95) {
            graphics.setColor(angryColor);
        }
        graphics.drawString(String.format("IRQ: %d%%", irqUsage), 140, 610);
        graphics.setColor(normalColor);
        graphics.drawString(String.format("FPS: %d", fps), 220 ,610);
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
