package com.pjfsw.sixfiveoto;

public class CpuStatistics {
    long irqCycles = 0;
    long normalCycles = 0;

    private static final long MAX = 1_000_000;
    private double speed;

    public long irqUsage() {
        long i = irqCycles;
        long n = normalCycles;
        if (irqCycles > MAX || normalCycles > MAX) {
            irqCycles/=2;
            normalCycles/=2;
        }
        if (i > n) {
            return 100;
        }
        return n > 0 ? 100 * i/n : 0;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public double getSpeed() {
        return speed;
    }
}
