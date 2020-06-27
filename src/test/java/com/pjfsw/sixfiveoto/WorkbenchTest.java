package com.pjfsw.sixfiveoto;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class WorkbenchTest {
    @Test
    public void basicTest() {
        Workbench wb = new Workbench(emptyList());
        wb.poke(0x200,10);
        assertEquals(10, wb.peek(0x200));
    }
}
