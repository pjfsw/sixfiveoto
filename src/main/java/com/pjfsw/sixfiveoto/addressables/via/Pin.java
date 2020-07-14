package com.pjfsw.sixfiveoto.addressables.via;

public class Pin {
    public volatile Boolean value;

    public static Pin input() {
        Pin pin = new Pin();
        pin.value = false;
        return pin;
    }

    public static Pin output() {
        return new Pin();
    }

}
