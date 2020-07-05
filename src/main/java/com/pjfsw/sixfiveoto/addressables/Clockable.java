package com.pjfsw.sixfiveoto.addressables;

/**
 * An entity that is run by a system clock
 */
public interface Clockable {
    /**
     * Advance the unit a specified number of cycles.
     *
     * @param cycles the number of cycles to advance, greater than zero
     */
    void next(int cycles);
}
