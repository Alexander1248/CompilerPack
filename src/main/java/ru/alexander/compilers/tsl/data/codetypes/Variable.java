package ru.alexander.compilers.tsl.data.codetypes;

public class Variable {
    public String cipher;
    public int creationCall;
    public int lastCall;

    public int layerIndex;

    public Variable(String cipher) {
        this.cipher = cipher;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "cipher='" + cipher + '\'' +
                ", creationCall=" + creationCall +
                ", lastCall=" + lastCall +
                '}';
    }
}
