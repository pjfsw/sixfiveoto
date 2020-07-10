package com.pjfsw.sixfiveoto.gti;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Function;
import java.util.function.Supplier;

public class GtiTerminal {
    private final Supplier<Integer> input;
    private final Function<Integer, Boolean> output;
    private final InputStream is;
    private final OutputStream os;
    private int nextByteToCpu = -1;
    private int nextByteToUser = -1;

    public GtiTerminal(Supplier<Integer> input, Function<Integer,Boolean> output, InputStream is, OutputStream os) {
        this.input = input;
        this.output = output;
        this.is = is;
        this.os = os;
    }

    public void poll() throws IOException {
        if (nextByteToCpu == -1) {
            if (is.available() > 0) {
                nextByteToCpu = is.read();
            }
        }
        if (nextByteToCpu != -1 && output.apply(nextByteToCpu)) {
            nextByteToCpu = -1;
        }
        if (nextByteToUser == -1) {
            nextByteToUser = input.get();
        }
        if (nextByteToUser != -1) {
            os.write(nextByteToUser);
            nextByteToUser = -1;
        }
    }
}
