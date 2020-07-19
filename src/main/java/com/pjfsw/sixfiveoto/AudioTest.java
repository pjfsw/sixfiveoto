package com.pjfsw.sixfiveoto;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioTest {
    private static final int BUFFER_SIZE_SAMPLES = 512;
    private static final int SAMPLE_RATE = 48000;

    byte[] createBuffer(int t, double freq) {
        double period = (double)SAMPLE_RATE / freq;
        byte[] buffer = new byte[BUFFER_SIZE_SAMPLES];
        for (int i = 0; i < BUFFER_SIZE_SAMPLES; i++) {
            int angle = t % (int)period;
            long sample = (long)(100 * Math.sin(angle * Math.PI / (2 * period)));
            t++;
            buffer[i] = (byte)sample;
            //System.out.println(String.format(
//                "Sample: %04X Lo: %02X", sample, buffer[i]));
        }
        return buffer;
    }

    void play() throws LineUnavailableException {
        AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
        try (SourceDataLine line = AudioSystem.getSourceDataLine(af)) {
            line.open(af, BUFFER_SIZE_SAMPLES);
            line.start();
            long samplesToWrite = SAMPLE_RATE;
            int t = 0;
            while (samplesToWrite > 0) {
                if (line.available() >= BUFFER_SIZE_SAMPLES) {
                    byte[] buffer = createBuffer(t, 440);
                    int bytesWritten = line.write(buffer, 0, buffer.length);
                    samplesToWrite -= bytesWritten;
                    System.out.println(String.format("%d %d", buffer.length, bytesWritten));
                    t += bytesWritten;
                }
            }
        }
    }
    public static void main(String[] args) {
        try {
            new AudioTest().play();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
