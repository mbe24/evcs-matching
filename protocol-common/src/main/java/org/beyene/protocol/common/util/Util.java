package org.beyene.protocol.common.util;

import java.security.SecureRandom;
import java.util.TimerTask;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

public final class Util {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Util() {
        throw new AssertionError("no instances allowed");
    }

    public static String generateString(int length) {
        return generateString(ALPHABET, length, RANDOM::nextInt);
    }

    // nextInt = (bound) -> [0, bound)
    private static String generateString(String source, int length, IntFunction<Integer> nextInt) {
        StringBuilder sb = new StringBuilder();
        IntStream.generate(source::length)
                .boxed()
                .limit(length)
                .map(nextInt::apply)
                .map(source::charAt)
                .forEach(sb::append);

        return sb.toString();
    }

    public static TimerTask toTimerTask(Runnable r) {
        return new TimerTask() {
            @Override
            public void run() {
                r.run();
            }
        };
    }
}
