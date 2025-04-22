package paxos;

import paxos.utils.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class Proposer {
    private final ExecutorService executorService;
    private final int id;
    private final Networking networking;
    private final Set<Integer> acceptors;
    private Value value;
    private int round;

    public Proposer(int id, Networking networking, Value value) {
        this.executorService = Executors.newVirtualThreadPerTaskExecutor();
        this.id = id;
        this.networking = networking;
        this.acceptors = new HashSet<>();
        this.value = value;
        this.round = RoundGenerator.getNextRound();
    }

    public void addAcceptor(int id) {
        this.acceptors.add(id);
    }

    public boolean becomeMajority(ArrayList<Message> presents) {
        long valid = presents.stream().filter(Message::isOk).count();
        return valid >= ((acceptors.size() / 2) + 1);
    }

    public void run() {
        try {
            boolean done = false;
            while (!done) {
                Value maxValue = this.value;
                int maxRound = this.round;

                // Phase 1: prepare
                Message prepareMessage = Message.newPrepareMessage(id, round);
                broadcast(prepareMessage);

                // Phase 1: promise
                ArrayList<Message> promiseMessage = waitingForMessages(MessageType.PROMISE);
                if (becomeMajority(promiseMessage)) {
                    for (Message promise : promiseMessage) {
                        if (promise.getValueVersion() > maxRound) {
                            maxValue = promise.getValue();
                            maxRound = promise.getValueVersion();
                        }
                    }

                    if (maxRound == this.round) {
                        // Phase 2: propose
                        Message proposeMessage = Message.newProposeMessage(this.id, round, value);
                        broadcast(proposeMessage);

                        // Phase 2: accept
                        ArrayList<Message> acceptMessages = waitingForMessages(MessageType.ACCEPT);
                        for (Message accept : acceptMessages) {
                            if (accept.getValueVersion() > maxRound) {
                                maxRound = accept.getValueVersion();
                            }
                        }

                        if (becomeMajority(acceptMessages)) {
                            done = true;
                            System.out.printf("[Proposer %d] write successfully.\n", id);
                        }
                    } else {
                        this.round = RoundGenerator.getNextRound();
                        this.value = maxValue;
                    }
                } else {
                    this.round = RoundGenerator.getNextRound();
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException | TimeoutException ignored) {
        }

        executorService.shutdown();
        executorService.shutdownNow();
    }

    public void broadcast(Message message) {
        this.acceptors.forEach(to -> {
            this.networking.sendMessage(message, to);
        });
    }

    public ArrayList<Message> waitingForMessages(MessageType type)
            throws ExecutionException, InterruptedException, TimeoutException {
        ArrayList<Message> messages = new ArrayList<>();
        Future<?> submitted = executorService.submit(() -> {
            for (; ; ) {
                Message message = networking.receiveMessage(this.id);
                if (message.getType() == type) {
                    messages.add(message);
                }
            }
        });

        try {
            submitted.get(100, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException | TimeoutException ignored) {
        } finally {
            submitted.cancel(true);
        }

        return messages;
    }
}
