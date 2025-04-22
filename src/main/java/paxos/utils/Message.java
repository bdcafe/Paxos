package paxos.utils;

public class Message {
    private final MessageType type;
    private final int from;
    private final int round;
    private final Value value;
    private final int valueVersion;
    private final boolean ok;

    private Message(MessageType type, int from, int round, Value value, int valueVersion, boolean ok) {
        this.type = type;
        this.from = from;
        this.round = round;
        this.value = value;
        this.valueVersion = valueVersion;
        this.ok = ok;
    }

    public static Message newPrepareMessage(int from, int round) {
        return new Message(MessageType.PREPARE, from, round, null, 0, true);
    }

    public static Message newPromiseMessage(int from, int round, Value value, int valueVersion, boolean ok) {
        return new Message(MessageType.PROMISE, from, round, value, valueVersion, ok);
    }

    public static Message newProposeMessage(int from, int round, Value value) {
        return new Message(MessageType.PROPOSE, from, round, value, round, true);
    }

    public static Message newAcceptMessage(int from, Value value, int valueVersion, boolean ok) {
        return new Message(MessageType.ACCEPT, from, valueVersion, value, valueVersion, ok);
    }

    public boolean isOk() {
        return ok;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type).append(" { ");
        sb.append("from = ").append(from);
        sb.append(", round = ").append(round);
        switch (type) {
            case PROMISE, PROPOSE, ACCEPT -> {
                sb.append(", value = ").append(value);
                sb.append(", valueVersion = ").append(valueVersion);
            }
        }
        sb.append(", ok = ").append(ok);
        sb.append(" }");

        return sb.toString();
    }

    public MessageType getType() {
        return type;
    }

    public int getFrom() {
        return from;
    }

    public int getRound() {
        return round;
    }

    public Value getValue() {
        return value;
    }

    public int getValueVersion() {
        return valueVersion;
    }
}
