package com.pjfsw.sixfiveoto.lcd;

import java.awt.Graphics;

import com.pjfsw.sixfiveoto.addressables.Clockable;
import com.pjfsw.sixfiveoto.addressables.Drawable;
import com.pjfsw.sixfiveoto.addressables.Resettable;

public class Lcd implements Resettable, Clockable, Drawable {
    @Override
    public void next(final int cycles) {

    }

    @Override
    public void draw(final Graphics graphics) {

    }

    @Override
    public void reset(final boolean hardReset) {

    }
}
