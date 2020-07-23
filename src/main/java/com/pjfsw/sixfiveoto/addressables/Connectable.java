package com.pjfsw.sixfiveoto.addressables;

import com.pjfsw.sixfiveoto.addressables.via.Pin;

public interface Connectable {
    Pin getPin(String pinName);
}
