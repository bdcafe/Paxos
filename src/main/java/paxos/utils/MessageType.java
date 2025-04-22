package paxos.utils;

public enum MessageType {
    PREPARE("Phase 1: Proposer sends to acceptors"),
    PROMISE("Phase 1: Acceptor sends to the proposer"),
    PROPOSE("Phase 2: Proposer sends to acceptors"),
    ACCEPT("Phase 2: Acceptor sends to the proposer");

    MessageType(String description) {
    }
}
