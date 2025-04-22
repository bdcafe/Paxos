package paxos.utils;

public record Value(String value) {

    @Override
    public String toString() {
        return value == null ? null : "\"" + value + "\"";
    }
}
