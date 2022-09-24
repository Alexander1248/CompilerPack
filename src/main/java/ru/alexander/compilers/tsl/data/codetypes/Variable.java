package ru.alexander.compilers.tsl.data.codetypes;

public class Variable {
    public String cipher;

    public Variable(String cipher) {
        this.cipher = cipher;
    }

    @Override
    public String toString() {
        return "Variable{" +
                "cipher='" + cipher + "'}";
    }
}
