package com.pjfsw.sixfiveoto.audio;

public class FunctionGenerator {
    private FunctionGenerator() {
        // Statics only
    }

    static double generateSaw(int sample, int sampleRate, double f, double cutoff) {
        double sign = -1;
        double out = 0;
        double w = sample * f * 2.0 * Math.PI / (double)sampleRate;
        //System.err.printf("sample %d w %.2f%n", sample, w);
        for (int harmonic = 1; harmonic < 400; harmonic++) {
            double f2 = harmonic * f;
            if (f2 < cutoff) {
                double amp = 0.6 * sign * Math.sin(harmonic * w) / harmonic;
                out += amp;
                sign = -sign;
            }
        }
        return out;
    }

    static double generateSquare(int sample, int sampleRate, double f, double cutoff) {
        double w = sample * f * 2.0 * Math.PI / (double)sampleRate;
        double out = 0;
        for (int harmonic = 1; harmonic < 400; harmonic += 2) {
            double f2 = harmonic * f;
            if (f2 < cutoff) {
                double amp = Math.sin(harmonic * w)/harmonic;
                out += amp;
            }
        }
        return out;
    }
}
