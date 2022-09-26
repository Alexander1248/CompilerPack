package ru.alexander.compilers.tsl.data;

import java.util.Arrays;

public class Function {
    public String name;
    public String[] inputNames;
    public String[] inputTypes;

    public int codeStart;
    public int codeEnd;

    @Override
    public String toString() {
        return "Function{" +
                "name='" + name + '\'' +
                ", input=" + (inputNames != null ? Arrays.toString(inputNames) : "null") +
                '}';
    }
}
