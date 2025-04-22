package paxos.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class RoundGenerator {
    private static final AtomicInteger round = new AtomicInteger(0);

    public static int getNextRound() {
        return round.incrementAndGet();
    }
}
