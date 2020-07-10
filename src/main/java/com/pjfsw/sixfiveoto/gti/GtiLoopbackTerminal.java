package com.pjfsw.sixfiveoto.gti;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

public class GtiLoopbackTerminal implements Function<Integer, Boolean>, Supplier<Integer> {

    private Integer value;

    public void run() throws IOException {
        GtiTcpTerminal terminal = new GtiTcpTerminal(Executors.newSingleThreadExecutor(), this, this,
            (connected)->{});
        terminal.start();
        while(true) {
            terminal.next(1);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Boolean apply(final Integer integer) {
        this.value = integer;
        return true;
    }

    @Override
    public Integer get() {
        int valueToGet = value != null ? value : -1;
        value = null;
        return valueToGet;
    }

    public static void main(String[] args) {
        try {
            new GtiLoopbackTerminal().run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
