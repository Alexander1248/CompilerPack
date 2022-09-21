package ru.alexander.compilers;

import ru.alexander.compilers.tsl.TSLTools;

import java.io.File;

public class App {
    public static void main(String[] args) {
        TSLTools.compileFile(new File("test.txt"));
        TSLTools.runCompiledFile(new File("test_4738.tsl"));
    }
}
