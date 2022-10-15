package com.pjfsw.sixfiveoto;

import static java.util.Collections.emptyList;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.registers.Registers;

public class Debugger implements Drawable {
    private static final int ROWSIZE = 16;
    private static final int ROWS_OFFSET = 2 * ROWSIZE;
    private static final int ROWS = 4;
    private static final int WIDTH = 320;
    private static final int HEIGHT = ROWS_OFFSET + ( 2 + ROWS) * ROWSIZE;
    private final Registers registers;
    private final Map<Integer, String> symbols;
    private final Font font;
    private boolean enabled;
    private String registerValues = "";
    private final AtomicReference<List<Row>> instructions = new AtomicReference<>(emptyList());

    public Debugger(Registers registers, Map<Integer, String> symbols) {
        this.registers = registers;
        this.symbols = symbols;
        this.font = new Font("Courier", Font.PLAIN, 16);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void update(Disassembler disassembler) {
        this.registerValues = registers.toString();
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < ROWS; i++) {
            rows.add(new Row(disassembler.getPc(), disassembler.disassemble()));
        }
        this.instructions.set(rows);
    }

    @Override
    public void draw(final Graphics graphics) {
        if (!enabled) {
            return;
        }

        Graphics2D g2 = (Graphics2D)graphics;
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(0,0, WIDTH, HEIGHT);
        g2.setFont(font);
        int base = g2.getFontMetrics().getAscent();
        g2.setColor(Color.WHITE);

        int i = 0;
        for (Row row : this.instructions.get()) {
            String instr = String.format("%s %s",
                row.instruction,
                symbols.containsKey(row.address)?
                    "; " + symbols.get(row.address) : "");


            g2.drawString(String.format("$%04X", row.address),0,base + ROWS_OFFSET+i);
            g2.drawString(instr, 96, base + ROWS_OFFSET + i);
            i+=ROWSIZE;
            g2.setColor(Color.GRAY);
        }
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawString(registerValues, 0,base + 108);
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    private record Row(int address, String instruction) {
    }
}
