package paxos;

import paxos.utils.Message;
import paxos.utils.Networking;
import paxos.utils.Value;

public class Acceptor {
    private final int id;
    private final Networking networking;
    private Value value;
    private int valueVersion;
    private int round;

    public Acceptor(int id, Networking networking) {
        this.id = id;
        this.networking = networking;
    }

    public String status() {
        return String.format("[Acceptor %d] { round = %d, value = %s, valueVersion = %d }\n", id, round, value,
                             valueVersion);
    }

    public void run() {
        for (; ; ) {
            try {
                Message message = networking.receiveMessage(this.id);
                if (message != null) {
                    System.out.printf("[Acceptor %d] received %s\n", id, message);
                    switch (message.getType()) {
                        case PREPARE -> {
                            handlePrepare(message);
                        }
                        case PROPOSE -> {
                            handlePropose(message);
                        }
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    synchronized private void handlePrepare(Message message) {
        int to = message.getFrom();

        if (round < message.getRound()) {
            round = message.getRound();
            message = Message.newPromiseMessage(id, round, value, valueVersion, true);
        } else {
            message = Message.newPromiseMessage(id, round, value, valueVersion, false);
        }

        System.out.printf("[Acceptor %d] sends %s to [Proposer %d]\n", id, message, to);
        this.networking.sendMessage(message, to);
    }

    synchronized public void handlePropose(Message message) {
        int to = message.getFrom();

        if (round == message.getRound()) {
            value = message.getValue();
            valueVersion = message.getValueVersion();
            message = Message.newAcceptMessage(id, value, valueVersion, true);
        } else {
            System.out.printf("[Acceptor %d] { round = %d } refuses %s\n", id, round, message);
            message = Message.newAcceptMessage(id, value, valueVersion, false);
        }

        System.out.printf("[Acceptor %d] sends %s to [Proposer %d]\n", id, message, to);
        this.networking.sendMessage(message, to);
    }
}
