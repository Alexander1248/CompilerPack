package ru.alexander.compilers;

import ru.alexander.compilers.tsl.TSLTools;

import java.io.File;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        String str = new Scanner(System.in).next();

        if (TSLTools.compileWithTSLAssembler(new File(str + ".txt")))
            TSLTools.runCompiledFile(new File(str + "_0.tsl"), new int[]{}, new int[]{4});
    }
}
