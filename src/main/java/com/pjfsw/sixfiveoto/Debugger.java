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
    private final Registers registers;
    private final Map<Integer, String> symbols;
    private final Font font;
    private boolean enabled;
    private int pc;
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
        Graphics2D g2 = (Graphics2D)graphics;

        if (enabled) {
            g2.setFont(font);
            g2.setColor(Color.DARK_GRAY);

            g2.fillRect(0,ROWS_OFFSET-font.getSize(), 320, ROWSIZE+4);
            g2.setColor(Color.WHITE);

            int i = 0;
            for (Row row : this.instructions.get()) {
                if (symbols.containsKey(row.address)) {
                    g2.drawString("; " + symbols.get(row.address), 224, ROWS_OFFSET+i);
                }
                g2.drawString(String.format("$%04X", row.address),0,ROWS_OFFSET+i);
                g2.drawString(row.instruction, 96,ROWS_OFFSET+i);
                i+=ROWSIZE;
            }
            g2.drawString(registerValues, 0,112);
        }
    }

    private static class Row {
        private final int address;
        private final String instruction;

        public Row(int address, String instruction) {
            this.address = address;
            this.instruction = instruction;
        }
    }
}
