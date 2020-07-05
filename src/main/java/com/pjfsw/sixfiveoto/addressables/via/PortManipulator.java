package com.pjfsw.sixfiveoto.addressables.via;

public class PortManipulator {
    private PortManipulator() {
        // only static methods
    }
    public static int combineDdrOutIn(int ddr, int outputValue, int inputValue) {
        return (inputValue & (ddr ^ 0xFF)) | (outputValue & ddr);
    }
}
