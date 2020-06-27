package com.pjfsw.sixfiveoto;

import java.util.HashMap;
import java.util.Map;

import com.pjfsw.sixfiveoto.addressables.Peeker;
import com.pjfsw.sixfiveoto.addressables.Poker;

public class AddressDecoder implements Peeker, Poker {
    private final Map<Integer, Poker> consumers = new HashMap<>();
    private final Map<Integer, Peeker> functions = new HashMap<>();

    /**
     * Map a poker to the specified high byte in memory
     *
     * @param poker The poker to map
     * @param firstHighByte The first high byte value which selects the poker
     * @param lastHighByte The last high byte value which selects the poker
     */
    public void mapPoker(Poker poker, Integer firstHighByte, Integer lastHighByte) {
        for (int i = firstHighByte; i <= lastHighByte; i++) {
            consumers.put(i, poker);
        }
    }

    /**
     * Map a peeker to the specified high byte in memory
     *
     * @param peeker The function to map
     * @param firstHighByte The first high byte value which selects the peeker
     * @param lastHighByte The last high byte value which selects the peeker
     */
    public void mapPeeker(Peeker peeker, Integer firstHighByte, Integer lastHighByte) {
        for (int i = firstHighByte; i <= lastHighByte; i++) {
            functions.put(i, peeker);
        }
    }

    @Override
    public void poke(int address, int data) {
        consumers.getOrDefault(address, (a,d) -> {}).poke(address, data);

    }

    @Override
    public int peek(int address) {
        return functions.getOrDefault(address >> 8, (a) -> 0x00).peek(address);
    }
}
