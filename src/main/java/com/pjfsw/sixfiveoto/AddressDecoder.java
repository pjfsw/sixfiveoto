package com.pjfsw.sixfiveoto;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class AddressDecoder implements Consumer<Integer>, Function<Integer, Integer> {
    private final Map<Integer, Consumer<Integer>> consumers = new HashMap<>();
    private final Map<Integer, Function<Integer, Integer>> functions = new HashMap<>();

    /**
     * Map a consumer to the specified high byte in memory
     *
     * @param consumer The consumer to map
     * @param firstHighByte The first high byte value which selects the consumer
     * @param lastHighByte The last high byte value which selects the consumer
     */
    public void mapConsumer(Consumer<Integer> consumer, Integer firstHighByte, Integer lastHighByte) {
        for (int i = firstHighByte; i <= lastHighByte; i++) {
            consumers.put(i, consumer);
        }
    }

    /**
     * Map a function to the specified high byte in memory
     *
     * @param function The function to map
     * @param firstHighByte The first high byte value which selects the function
     * @param lastHighByte The last high byte value which selects the function
     */
    public void mapFunction(Function<Integer, Integer> function, Integer firstHighByte, Integer lastHighByte) {
        for (int i = firstHighByte; i <= lastHighByte; i++) {
            functions.put(i, function);
        }
    }

    @Override
    public void accept(final Integer address) {
        consumers.getOrDefault(address, (x) -> {}).accept(address);

    }

    @Override
    public Integer apply(final Integer address) {
        return functions.getOrDefault(address >> 8, (x) -> 0x00).apply(address);
    }
}
