package ru.alexander.compilers.tsl.data.codetypes;

import java.util.Arrays;

public class Function {
    public String name;
    public String[] input;

    public int codeStart;
    public int codeEnd;

    @Override
    public String toString() {
        return "Function{" +
                "name='" + name + '\'' +
                ", input=" + (input != null ? Arrays.toString(input) : "null") +
                '}';
    }
}
