package ru.alexander.compilers;

import ru.alexander.compilers.tsl.TSLTools;

import java.io.File;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        String code = """
                def vtex(var n) {
                    curr = 1;
                    var prev = 1;
                    
                    for (var i = 0; i < n; i++) {
                        var buff = curr + prev;
                        prev = curr;
                        curr = buff;
                    }
                }
                """;
        TSLTools.testAetherCode(code, true);
    }
}
