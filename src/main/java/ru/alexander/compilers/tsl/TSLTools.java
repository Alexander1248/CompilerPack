package ru.alexander.compilers.tsl;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.alexander.compilers.tsl.compilers.AetherTSL;
import ru.alexander.compilers.tsl.compilers.TSLAssembler;
import ru.alexander.compilers.tsl.data.TSCode;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TSLTools {
    private TSLTools() {}
    public static boolean save(@NotNull TSCode code, @NotNull File file) {
        try {
            Gson gson = new Gson();
            FileWriter writer = new FileWriter(new File(file.getParent(),file.getName().split("\\.")[0] + "_" + code.modulationKey + ".tsl"));
            writer.write(Base64.getEncoder().encodeToString(gson.toJson(code).getBytes(StandardCharsets.UTF_8)));
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

            return gson.fromJson(new String(Base64.getDecoder().decode(bytes), StandardCharsets.UTF_8), TSCode.class);
        } catch (IOException e) {
            System.out.println("File not found!");
            return null;
        }
    }

    public static boolean compileWithTSLAssembler(File file) {
        try {
            FileInputStream reader = new FileInputStream(file);
            String[] code = new String(reader.readAllBytes()).split("\r\n");

            reader.close();
            TSCode compile = TSLAssembler.builder(new ArrayList<>(Arrays.asList(code)));
            save(compile, file);
            return true;
        } catch (IOException ignored) {
            System.out.println("File not found!");
            return false;
        }
    }

    public static void runCompiledFile(File file, int[] in,  int[] out) {
        TSCode load = load(file);
        if (load != null) {
            TSLVirtualMachine vm = new TSLVirtualMachine(load);
            Scanner scanner = new Scanner(System.in);
            Locale.setDefault(Locale.ENGLISH);

            System.out.println("Enter values: ");
            for (int i = 0; i < in.length; i++)
                vm.setVariable(in[i], scanner.nextFloat());
            System.out.println("Running...");

            vm.runCode();

            for (int i = 0; i < out.length; i++)
                System.out.print(vm.getVariable(out[i]) + " ");
        }
        else System.out.println("File not found!");
    }
    public static void testCode(TSCode code, int @NotNull [] in, int[] out) {
        TSLVirtualMachine vm = new TSLVirtualMachine(code);
        Scanner scanner = new Scanner(System.in);
        Locale.setDefault(Locale.ENGLISH);

        System.out.println("Enter values: ");
        for (int i = 0; i < in.length; i++)
            vm.setVariable(in[i], scanner.nextFloat());
        System.out.println("Running...");

        vm.runCode();

        for (int i = 0; i < out.length; i++)
            System.out.print(vm.getVariable(out[i]) + " ");
    }

}
