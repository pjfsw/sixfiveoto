package com.pjfsw.sixfiveoto.addressables;

/**
 * A unit that needs to/can be reset
 */
public interface Resettable {
    /**
     * Reset the unit
     *
     * @param hardReset Turn off and on the system, restoring all defaults
     */
    void reset(boolean hardReset);
}
