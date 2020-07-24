package com.pjfsw.sixfiveoto;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Mixer.Info;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

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

    void playClip() throws LineUnavailableException, IOException, InterruptedException,
        UnsupportedAudioFileException {

        AudioFormat af = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

        try (
            AudioInputStream sound1 = AudioSystem.getAudioInputStream(new File("/Users/johanfr/priv/eclipsecpp/pixla/poop2.wav"));
            Clip clip1 = AudioSystem.getClip();
            Clip clip2 = AudioSystem.getClip()
        ) {
            clip1.open(sound1);
            int samples = SAMPLE_RATE/880;
            byte[] beep = new byte[samples*2];
            for (int i = 0; i < samples; i++) {
                int v = 0;
                for (int harmonics = 1; harmonics < 20; harmonics+=2) {
                    v += (int)(28000.0 * Math.sin(harmonics*2*i*Math.PI/samples) / harmonics);
                }

                beep[i*2] = (byte)(v & 0xff);
                beep[i*2+1] = (byte)(v >> 8);
            }

            clip2.open(af, beep, 0, beep.length);
            FloatControl volume = (FloatControl)clip2.getControl(FloatControl.Type.MASTER_GAIN);
            volume.setValue(-6.0f);
            clip1.start();
            clip2.loop(SAMPLE_RATE/(10*samples));
            Thread.sleep(1000);
            clip2.stop();
            Thread.sleep(1000);
            clip1.stop();
        }
    }

    public static void main(String[] args) {
        try {
            new AudioTest().playClip();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
