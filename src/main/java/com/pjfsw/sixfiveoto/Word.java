package com.pjfsw.sixfiveoto;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

public final class Word {
    private Word() {
        // only static methods
    }

    public static Collection<Integer> wordToBytes(Integer word) {
        return ImmutableList.of(word & 0xFF, word >> 8);
    }

    public static int lo(int value) {
        return value & 0xFF;
    }

    public static int hi(int value) {
        return value >> 8;
    }

}
