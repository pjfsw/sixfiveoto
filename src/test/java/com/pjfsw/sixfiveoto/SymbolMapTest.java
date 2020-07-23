package com.pjfsw.sixfiveoto;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class SymbolMapTest {
    @Test
    public void test() {
        assertTrue(SymbolMap.SYMBOL_RE.matcher(".label game_over=$1c20").matches());
        assertTrue(SymbolMap.SYMBOL_RE.matcher(".label title_screen=$30d {").matches());
        assertFalse(SymbolMap.SYMBOL_RE.matcher("}").matches());
    }
}
