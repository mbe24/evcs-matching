package org.beyene.protocol.common.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.stream.IntStream;

public final class Data {

    private Data() {
        throw new AssertionError("no instances allowed");
    }

    public static <T, K> List<T> getNext(List<T> objects, K key, Function<T, K> toKey) {
        // is no item is found, return all objects (hence -1)
        int index = indexOf(objects, key, toKey).orElse(-1);

        // if last item is found, there is no next item
        List<T> result;
        if (index == objects.size() - 1)
            result = Collections.emptyList();
        else
            result = new ArrayList<>(objects.subList(index + 1, objects.size()));

        return result;
    }

    public static <T, K> OptionalInt indexOf(List<T> objects, K key, Function<T, K> toKey) {
        return IntStream.range(0, objects.size())
                .filter(i -> key.equals(toKey.apply(objects.get(i))))
                .findFirst();
    }
}
