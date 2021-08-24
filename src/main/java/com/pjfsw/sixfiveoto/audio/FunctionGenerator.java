package com.pjfsw.sixfiveoto.audio;

public class FunctionGenerator {
    private FunctionGenerator() {
        // Static only
    }

    /**
     * Generate output for a reverse sawtooth wave (https://en.wikipedia.org/wiki/Sawtooth_wave)
     *
     * @param sample the sample position relative to the sample rate
     * @param sampleRate  the sample rate
     * @param f the frequency to be generated
     * @param cutoff the harmonic cutoff frequency
     * @return output
     */
    static double generateSaw(int sample, int sampleRate, double f, double cutoff) {
        double sign = -1;
        double out = 0;
        double w = sample * f * 2.0 * Math.PI / (double)sampleRate;
        //System.err.printf("sample %d w %.2f%n", sample, w);
        for (int harmonic = 1;; harmonic++) {
            double f2 = harmonic * f;
            if (f2 > cutoff) {
                break;
            }
            double amp = 0.6 * sign * Math.sin(harmonic * w) / harmonic;
            out += amp;
            sign = -sign;
        }
        return out;
    }

    /**
     * Generate output for a square wave (https://en.wikipedia.org/wiki/Square_wave#Fourier_analysis)
     *
     * @param sample the sample position relative to the sample rate
     * @param sampleRate  the sample rate
     * @param f the frequency to be generated
     * @param cutoff the harmonic cutoff frequency
     * @return output
     */
    static double generateSquare(int sample, int sampleRate, double f, double cutoff) {
        double w = sample * f * 2.0 * Math.PI / (double)sampleRate;
        double out = 0;
        for (int harmonic = 1; ; harmonic += 2) {
            double f2 = harmonic * f;
            if (f2 > cutoff) {
                break;
            }
            double amp = Math.sin(harmonic * w)/harmonic;
            out += amp;
        }
        return out;
    }

    /**
     * Generate output for a triangle wave (https://en.wikipedia.org/wiki/Triangle_wave#Harmonics)
     *
     * @param sample the sample position relative to the sample rate
     * @param sampleRate  the sample rate
     * @param f the frequency to be generated
     * @param cutoff the harmonic cutoff frequency
     * @return output
     */
    static double generateTriangle(int sample, int sampleRate, double f, double cutoff) {
        double w = sample * f * 2.0 * Math.PI / (double)sampleRate;
        double out = 0;
        double sign = -1;

        for (int harmonic = 0;; harmonic++) {
            int n = 2*harmonic+1;
            double f2 = harmonic * n;
            if (f2 > cutoff) {
                break;
            }
            out += sign * Math.pow(n, -2) * Math.sin(n * w);
        }
        out *= 0.81;
        return out;
    }
}
