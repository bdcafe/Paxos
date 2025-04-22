package paxos.utils;

import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Networking {
    private static Networking defaultNetworking;
    private final ConcurrentHashMap<Integer, ArrayBlockingQueue<Message>> recvQueue;
    private final Random random;

    private Networking() {
        recvQueue = new ConcurrentHashMap<>();
        random = new Random();
    }

    public static Networking getDefaultNetworking() {
        if (defaultNetworking == null) {
            defaultNetworking = new Networking();
        }

        return defaultNetworking;
    }

    public void createRecvQueue(int id) {
        recvQueue.put(id, new ArrayBlockingQueue<>(64));
    }

    public void sendMessage(Message message, int to) {
        if (random.nextInt(1000) > 100) {
            try {
                Thread.sleep(random.nextInt(100));
                recvQueue.get(to).offer(message);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Message receiveMessage(int id) throws InterruptedException {
        Message message = null;
        if (recvQueue.containsKey(id)) {
            message = recvQueue.get(id).take();
        }
        return message;
    }
}
