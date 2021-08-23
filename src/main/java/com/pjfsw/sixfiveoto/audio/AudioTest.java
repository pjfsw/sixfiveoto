package com.pjfsw.sixfiveoto.audio;

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

/**
 * Tests for simple audio generation for a sound chip.
 *
 * Control ideas:
 *
 * Channel waveform: Sawtooth / Triangle? (TBD)
 * Channel phase: on/off
 * Channel phase: 0-255
 * Channel frequency
 * Ramp length (release time from ramp offset to 0) - only a few values
 * Ramp offset (initial level after release)
 *
 */
public class AudioTest {
    private final Synth synth;
    private final JFrame frame;
    private volatile boolean done = false;
    private static final int SAMPLE_RATE = 44100;

    private static final int WAVE_TABLES = 8;
    private static final int WAVE_TABLE_BASE_SIZE = 8192;
    private static final float[][] sawWaveTable = initWaveTable(WAVE_TABLES, WAVE_TABLE_BASE_SIZE, FunctionGenerator::generateSaw);
    private static final float[][] triWaveTable = initWaveTable(WAVE_TABLES, WAVE_TABLE_BASE_SIZE, FunctionGenerator::generateTriangle);

    private static float[][] waveTable;

    public static void main(String[] args) {
        try {
            waveTable = sawWaveTable;
            new AudioTest().play();
            System.exit(0);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

    interface WaveFunction {
        double generateWave(int sample, int sampleRate, double f, double cutoff);
    }

    private static float[][] initWaveTable(int tables, int samples, WaveFunction function) {
        float[][] waveTable = new float[tables][];
        // First octave = C1 = 32.70
        // Second octave = C2 = 65.41
        // ...
        // Eight ocrta = C8 = 4186.01
        int totalSize = 0;
        for (int tableIndex = 0; tableIndex < tables; tableIndex++) {
            int length = samples / (1 << tableIndex);
            totalSize += length;
            float[] table = new float[length];
            for (int t = 0; t < length; t++) {
                // 19200/ 32 = 600
                table[t] = (float)function.generateWave(t, length, 1, (double)400/(1<<tableIndex));
            }
            waveTable[tableIndex] = table;
        }
        System.err.printf("Table total size %d entries%n", totalSize);
        return waveTable;
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
            .put(',', 0)
            .put('m',-1)
            .put('j',-2)
            .put('n',-3)
            .put('h',-4)
            .put('b',-5)
            .put('g',-6)
            .put('v',-7)
            .put('c',-8)
            .put('d',-9)
            .put('x',-10)
            .put('s',-11)
            .put('z',-12)
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
        private final int[][] channelSequence;
        private final double[] gain;
        private final int[] phaseMod;
        private int n = 0;
        private int offset;

        private final double[] ramp;
        private int[] rampPos;

        public Synth() {
            ramp = new double[16000];
            double att = 0.6;
            for (int i = 0; i < ramp.length; i++) {
                double d = 1;
                if (i > 0) {
                    d = att - att * (double)i/ramp.length;
                }
                ramp[i] = d;
            }
            freqTable = new double[128];
            note = new int[4];
            phaseMod = new  int[note.length];
            sequencePos = new int[note.length];
            int[] sequence1 = new int[]{60,60,0,60, 60,0,60,60, 0,60,60,0, 60,0,60,0 };
            int[] sequence2 = new int[]{63,63,0,63, 63,0,63,65, 0,65,65,0, 65,0,67,0 };
            int[] sequence3 = new int[]{70,70,0,70, 70,0,70,69, 0,69,69,0, 70,0,72,0 };
            int[] bassSequence = new int[] { 24,36,0,0, 36,0,36,24,0,24,36,0,39,0,41,43 };
            channelSequence = new int[note.length][];
            rampPos = new int[note.length];
            gain = new double[note.length];

            channelSequence[0] = sequence1;
            channelSequence[1] = sequence2;
            channelSequence[2] = sequence3;
            channelSequence[3] = bassSequence;
            for (int i = 0; i < note.length; i++) {
                rampPos[i] = -1;
                gain[i] = 0.4;
            }
            gain[note.length-1] = 0.9;

            for (int n = 0 ; n < 127; n++) {
                freqTable[n] = 440 * Math.pow(2, (double)(n-69)/12);
            }
            for (int i = 0; i < sequencePos.length; i++) {
                sequencePos[i] = 0;
                note[i] = channelSequence[i][sequencePos[i]];
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
            int note = this.note[i];
            double f = getFrequency(note);
            int table = (note-36)/12;
            if (table < 0) {
                table = 0;
            }
            if (table > waveTable.length-1) {
                table = waveTable.length-1;
            }
            int phaseOffset = 20;
            int phaseModSize = 255;
            int tableLength = waveTable[table].length;
            int dutyCycle = (phaseOffset + (phaseMod[i]>>8) % phaseModSize)%256;
            int offset = (int)(f * n * tableLength / SAMPLE_RATE);
            //out = FunctionGenerator.generateSaw(n, SAMPLE_RATE, f, 20000);
            float phase1 = waveTable[table][offset % tableLength];
            int offset2 = offset + dutyCycle * tableLength / 256;
            float phase2 = waveTable[table][offset2 % tableLength];
            //out = phase1;
            out = phase1-phase2;

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
                out += gain[i] * get(i);
                phaseMod[i]++;
            }
            out *= 0.1;
            if (out < -0.52 || out > 0.52) {
                System.err.printf("Audio exceeding -6 dBFS %.2f%n", out);
            }
            n++;
            return (short)(out * 32767.0);
        }

        public void tick() {
            for (int i = 0; i < sequencePos.length; i++) {
                int nextNote =  channelSequence[i][sequencePos[i]];
                if (nextNote > 0) {
                    phaseMod[i] = 0;
                    rampPos[i] = 0;
                    note[i] = nextNote + offset;
                    //System.err.printf("%d=%f Hz%n", note[i], getFrequency(note[i]));
                }
                sequencePos[i] = (sequencePos[i] + 1) % channelSequence[i].length;
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
                    int step = n % 6000;
                    if (step == 0) {
                        synth.tick();
                    } else if (step == 800) {
                        synth.off();
                    }
                    n++;
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
