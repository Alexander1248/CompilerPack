package ru.alexander.compilers.tsl;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.alexander.compilers.tsl.data.TSCode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Scanner;

public class TSLTools {
    private TSLTools() {}
    public static boolean save(@NotNull TSCode code, @NotNull File file) {
        try {
            Gson gson = new Gson();
            FileWriter writer = new FileWriter(new File(file.getParent(),file.getName().split("\\.")[0] + "_" + code.modulationKey + ".tsl"));
            writer.write(gson.toJson(code));
            writer.flush();
            writer.close();

            return true;
        } catch (IOException e) {
            System.out.println("IO error!");
            return false;
        }
    }
    public static @Nullable TSCode load(File file) {
        try {
            Gson gson = new Gson();
            FileInputStream reader = new FileInputStream(file);
            byte[] bytes = reader.readAllBytes();
            reader.close();

            return gson.fromJson(new String(bytes, StandardCharsets.UTF_8), TSCode.class);
        } catch (IOException e) {
            System.out.println("File not found!");
            return null;
        }
    }

    public static void compileFile(File file) {
        try {
            FileInputStream reader = new FileInputStream(file);
            String code = new String(reader.readAllBytes());
            reader.close();

            TSLCompiler compiler = new TSLCompiler();
            TSCode[] compile = compiler.compile(code);
            for (TSCode tsCode : compile) save(tsCode, file);
        } catch (IOException ignored) {
            System.out.println("File not found!");
        }
    }
    public static void runCompiledFile(File file) {
        TSCode load = load(file);
        if (load != null) {
            System.out.println("Enter values: ");
            Locale.setDefault(Locale.ENGLISH);
            Scanner scanner = new Scanner(System.in);

            float[] input = new float[load.ioIndexes.length];
            for (int i = 0; i < input.length; i++) input[i] = scanner.nextFloat();
            System.out.println("Running...");

            interpreter(load, input);
            for (int i = 0; i < input.length; i++) System.out.print(input[i] + " ");
        }
        else System.out.println("File not found!");
    }

    public static void interpreter(@NotNull TSCode code, float... input) {
        int i;
        float[] valueBuffer = new float[code.varBuffSize];
        for (i = 0; i < code.ioIndexes.length; i++)
            valueBuffer[code.ioIndexes[i]] = input[i];

        for (i = 0; i < code.script.length; i++)
            i = runScript(code.script, valueBuffer, i);

        for (i = 0; i < code.ioIndexes.length; i++)
            input[i] = valueBuffer[code.ioIndexes[i]];
    }

    private static int runScript(float @NotNull [] script, float[] valueBuffer, int i) {
        if (script[i] == 1) {
            valueBuffer[(int) script[i + 1]] = script[i + 2];
            i += 2;
        } else if (script[i] == 2) {
            valueBuffer[(int) (script[i + 1] + valueBuffer[(int) script[i + 2]])] = script[i + 3];
            i += 3;
        } else if (script[i] == 3) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] + valueBuffer[(int) script[i + 3]];
            i += 3;
        } else if (script[i] == 4) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] - valueBuffer[(int) script[i + 3]];
            i += 3;
        } else if (script[i] == 5) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] * valueBuffer[(int) script[i + 3]];
            i += 3;
        } else if (script[i] == 6) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] / valueBuffer[(int) script[i + 3]];
            i += 3;
        } else if (script[i] == 7) {
            valueBuffer[(int) script[i + 1]] = (float) Math.sin(valueBuffer[(int) script[i + 2]]);
            i += 2;
        } else if (script[i] == 8) {
            valueBuffer[(int) script[i + 1]] = (float) Math.cos(valueBuffer[(int) script[i + 2]]);
            i += 2;
        } else if (script[i] == 9) {
            valueBuffer[(int) script[i + 1]] = (float) Math.tan(valueBuffer[(int) script[i + 2]]);
            i += 2;
        } else if (script[i] == 10) {
            valueBuffer[(int) script[i + 1]] = (float) Math.pow(
                    valueBuffer[(int) script[i + 2]],
                    valueBuffer[(int) script[i + 3]]);
            i += 3;
        } else if (script[i] == 11) {
            valueBuffer[(int) script[i + 1]] = (float) Math.log(valueBuffer[(int) script[i + 2]]);
            i += 2;
        } else if (script[i] == 12) {
            int j = i + 1;
            i = j;
            int last = 0;
            while (valueBuffer[(int) script[i]] > 0.5) {
                do {
                    i = runScript(script, valueBuffer, i + 1);
                    last = Math.max(last, i);
                } while (i > 0);
                i = j;
            }
            i = last + 1;
        } else if (script[i] == 13) {
            return -1;
        } else if (script[i] == 14) {
            throw new RuntimeException("Exception: " + valueBuffer[(int) script[i + 1]]);
        } else if (script[i] == 15) {
            if (valueBuffer[(int) script[i + 1]] > 0.5) {
                i += runScript(script, valueBuffer, i + 1);
            }
        } else if (script[i] == 16) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] > valueBuffer[(int) script[i + 3]] ? 1 : 0;
            i += 3;
        } else if (script[i] == 17) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] < valueBuffer[(int) script[i + 3]] ? 1 : 0;
            i += 3;
        } else if (script[i] == 18) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] == valueBuffer[(int) script[i + 3]] ? 1 : 0;
            i += 3;
        } else if (script[i] == 19) {
            float a = valueBuffer[(int) script[i + 2]];
            float n = valueBuffer[(int) script[i + 3]];
            if (a > n) valueBuffer[(int) script[i + 1]] = n - a;
            else valueBuffer[(int) script[i + 1]] = a - n;
            i += 3;
        } else if (script[i] == 20) {
            float a = valueBuffer[(int) script[i + 3]];
            valueBuffer[(int) script[i + 1]] = (float) Math.cos(a);
            valueBuffer[(int) script[i + 2]] = (float) Math.sin(a);
            i += 3;
        } else if (script[i] == 21) {
            float a = valueBuffer[(int) script[i + 2]];
            int A = (int) a;
            float b = valueBuffer[(int) script[i + 3]];
            valueBuffer[(int) script[i + 1]] = (a - A) + A % ((int) b);
            i += 3;
        } else if (script[i] == 22) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] >= valueBuffer[(int) script[i + 3]] ? 1 : 0;
            i += 3;
        } else if (script[i] == 23) {
            valueBuffer[(int) script[i + 1]] = valueBuffer[(int) script[i + 2]] <= valueBuffer[(int) script[i + 3]] ? 1 : 0;
            i += 3;
        } else if (script[i] == 24) {
            valueBuffer[(int) script[i + 1]] = Math.abs(valueBuffer[(int) script[i + 2]]);
            i += 2;
        }
        return i;
    }

}
