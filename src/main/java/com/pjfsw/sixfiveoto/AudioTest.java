package com.pjfsw.sixfiveoto;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import com.google.common.collect.ImmutableMap;

public class AudioTest {
    private final Synth synth;
    private final JFrame frame;
    private volatile boolean done = false;
    private static final int SAMPLE_RATE = 44100;

    public static void main(String[] args) {
        try {
            new AudioTest().play();
            System.exit(0);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    private AudioTest() {
        this.frame = new JFrame();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        this.synth = new Synth();
        initKeyListener();
    }

    private void initKeyListener() {
        Map<Character,Integer> offsets = ImmutableMap.<Character,Integer>builder()
            .put('q', 0)
            .put('2', 1)
            .put('w', 2)
            .put('3', 3)
            .put('e', 4)
            .put('r', 5)
            .put('5', 6)
            .put('t', 7)
            .put('6', 8)
            .put('y', 9)
            .put('7', 10)
            .put('u', 11)
            .put('i', 12)
            .build();
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((event) -> {
            if (event.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }
            if (event.getID() == KeyEvent.KEY_PRESSED) {
                if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    done = true;
                }
                int offset = offsets.getOrDefault(event.getKeyChar(), 0);
                synth.setOffset(offset);
                System.err.println("PRESSED " + event.getKeyCode());
                return false;
            }
            return true;
        });
    }

    private void play() throws LineUnavailableException {
        AudioFormat af = new AudioFormat( (float )SAMPLE_RATE, 16, 1, true, false );
        try (SourceDataLine sdl = AudioSystem.getSourceDataLine( af )) {
            sdl.open();
            sdl.start();
            AudioMixer audioMixer = new AudioMixer(sdl, synth);
            ExecutorService executorService = Executors.newSingleThreadExecutor();
            Future<?> future =  executorService.submit(audioMixer);
            while (!done) {
                waitMs(10);
            }
            frame.dispose();
            audioMixer.stop();
            sdl.stop();
            future.cancel(true);
            executorService.shutdown();
        }
    }

    private static void waitMs(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private static class Synth {
        private final double[] freqTable;
        private final int[] sequencePos;
        private final int[] note;
        private final int[] bassSequence;
        private final int[][] channelSequence;
        private int n = 0;
        private final int[] sequence;
        private int offset;

        private final double[] ramp;
        private int[] rampPos;
        public Synth() {
            ramp = new double[4000];
            for (int i = 0; i < ramp.length; i++) {
                double d = 1 - (double)i/ramp.length;
                ramp[i] = d;
            }
            freqTable = new double[128];
            note = new int[4];
            sequencePos = new int[note.length];
            sequence = new int[]{48,60,63,67,70,72,75,60};
            bassSequence = new int[]{36,36,0,36,36,0,36,36};
            channelSequence = new int[note.length][];
            rampPos = new int[note.length];
            for (int i = 0; i < note.length-1; i++) {
                channelSequence[i] = sequence;
                rampPos[i] = -1;
            }
            channelSequence[note.length-1] = bassSequence;

            for (int n = 0 ; n < 127; n++) {
                freqTable[n] = 440 * Math.pow(2, (double)(n-69)/12);
            }
            for (int i = 0; i < sequencePos.length; i++) {
                sequencePos[i] = (i * 3) % sequence.length;
                note[i] = sequencePos[i];
            }
        }
        public double getFrequency(int i) {
            return freqTable[i];
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        private double get(int i) {
            double out = 0;
            double f = getFrequency(note[i]);
            double w = n * f * 2.0 * Math.PI / (double)SAMPLE_RATE;
            /* Square wave
            for (int harmonic = 1; harmonic < 16; harmonic += 2) {
                double amp = Math.sin(harmonic * w)/harmonic;
                out += amp;
            }*/
            double sign = -1;
            for (int harmonic = 1; harmonic < 30; harmonic++) {
                double f2 = harmonic * f;
                if (f2 < 18000) {
                    double amp = 0.6 * sign * Math.sin(harmonic * w) / harmonic;
                    out += amp;
                    sign = -sign;
                }
            }
            if (rampPos[i] >= 0) {
                out = out * ramp[rampPos[i]];
            } else {
                out = 0;
            }
            if (rampPos[i] > 0) {
                rampPos[i]++;
                if (rampPos[i] >= ramp.length) {
                    rampPos[i] = -1;
                }
            }

            return out;
        }

        public short get() {
            double out = 0;
            for (int i = 0; i < sequencePos.length; i++) {
                out += get(i);
            }
            out *= 0.12;
            if (out < -0.5 || out > 0.5) {
                System.err.printf("Audio exceeding -6 dBFS %.2f%n", out);
            }
            n++;
            return (short)(out * 32767.0);
        }

        public void tick() {
            for (int i = 0; i < sequencePos.length; i++) {
                sequencePos[i] = (sequencePos[i] + 1) % sequence.length;
                int nextNote =  channelSequence[i][sequencePos[i]];
                if (nextNote > 0) {
                    rampPos[i] = 0;
                    note[i] = nextNote + offset;
                }
            }
        }
        public void off() {
            for (int i = 0; i < note.length; i++) {
                if (rampPos[i] == 0) {
                    rampPos[i] = 1;
                }
            }
        }
    }

    private static class AudioMixer implements Runnable {
        private final SourceDataLine sdl;
        private final byte[] buf;
        private final Synth synth;
        private final long delay;
        private int n;
        public volatile boolean running = true;
        private int offset = 0;

        private static final int SAMPLES = 256;
        private static final int BUFFER_SIZE = SAMPLES * 2;

        public AudioMixer(SourceDataLine sdl, Synth synth) {
            this.sdl = sdl;
            this.synth = synth;
            buf = new byte[BUFFER_SIZE];
            delay = 1_000_000_000L * SAMPLES / SAMPLE_RATE;
            n = 0;
        }

        public void stop() {
            this.running = false;
        }

        private static void waitNanos(long ns) {
            try {
                Thread.sleep(ns/1_000_000, (int)ns % 1_000_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (running) {
                long nanos = System.nanoTime();
                for (int sample = 0; sample < SAMPLES; sample++) {
                    n++;
                    int step = n % 8000;
                    if (step == 0) {
                        synth.tick();
                    } else if (step == 1000) {
                        synth.off();
                    }
                    short out = synth.get();
                    buf[sample * 2] = (byte)(out & 0xFF);
                    buf[sample * 2 + 1] = (byte)(out >> 8);
                }
                sdl.write( buf, 0, BUFFER_SIZE );
                long elapsedNanos = System.nanoTime() - nanos;
                offset += (delay-elapsedNanos);
                if (offset > 10000) {
                    waitNanos(10000);
                    offset-=10000;
                }
            }
        }
    }
}
