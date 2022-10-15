package com.pjfsw.sixfiveoto.addressables;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.pjfsw.sixfiveoto.CpuStatistics;

public class Screen {
    private static final int WAIT_PERIOD = 1_000 / 60;
    private final JFrame frame;
    private final CpuStatistics cpuStatistics;
    private final Font font;
    private final int leftOffset;
    private final int topOffset;
    private final float scaling;
    private final int screenHeight;
    private final int screenWidth;
    private volatile boolean running = true;
    private int fps = 0;
    private final List<PositionedDrawable> drawables = new ArrayList<>();

    public Screen(CpuStatistics cpuStatistics, float scaling, int screenWidth, int screenHeight) {
        this.cpuStatistics = Objects.requireNonNull(cpuStatistics);
        this.scaling = scaling;
        this.screenHeight = screenHeight;
        this.screenWidth = screenWidth;

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();
        GraphicsConfiguration gc = gs[0].getConfigurations()[0];
        this.font = new Font("Courier", Font.PLAIN, 14);

        frame = new JFrame("A Fine Emulator of 65C02", gc);

        frame.setPreferredSize(new Dimension((int)(scaling*screenWidth), (int)(scaling*screenHeight)));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        Insets insets = frame.getInsets();
        leftOffset = insets.left;
        topOffset = insets.top;
        int bottomOffset = insets.bottom;
        int rightOffset = insets.right;
        frame.setPreferredSize(new Dimension(
            (int)(scaling*screenWidth) + leftOffset + rightOffset,
            (int)(scaling*screenHeight) + topOffset + bottomOffset)
        );
        frame.pack();
        frame.setLocationRelativeTo(null);

        frame.createBufferStrategy(2);
        frame.setAlwaysOnTop(true);
        frame.setAlwaysOnTop(false);
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
            g2.fillRect(0,0, screenWidth*2, screenHeight*2);
            g2.translate(leftOffset, topOffset * 2);
            g2.scale(scaling, scaling);
            draw(g2);
            graphics.dispose();
            frames++;
            strategy.show();
            long wait = WAIT_PERIOD - (System.currentTimeMillis() - lastTime);
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
        Graphics g = graphics.create();
        g.setFont(font);
        int base = graphics.getFontMetrics().getAscent();
        int statusPos = base + (int)(screenHeight * scaling);
        int x = 800;
        g.setColor(normalColor);
        g.drawString(String.format("CPU: %.2f MHz", cpuStatistics.getSpeed()), x + 2, statusPos);
        long irqUsage = cpuStatistics.irqUsage();
        if (irqUsage > 0) {
            g.setColor(angryColor);
        }
        g.drawString(String.format("IRQ: %d%%", irqUsage), x + 140, statusPos);
        g.setColor(normalColor);
        g.drawString(String.format("FPS: %d", fps), x + 220, statusPos);
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
