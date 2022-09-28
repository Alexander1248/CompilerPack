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

    public static boolean compileWithAssembler(File file) {
        try {
            FileInputStream reader = new FileInputStream(file);
            String code = new String(reader.readAllBytes());

            reader.close();
            TSLAssembler assembler = new TSLAssembler();
            TSCode compile = assembler.compile(code);
            save(compile, file);
            return true;
        } catch (IOException ignored) {
            System.out.println("File not found!");
            return false;
        }
    }
    public static boolean compileWithAether(File file, boolean printCompiledScript) {
        try {
            FileInputStream reader = new FileInputStream(file);
            String code = new String(reader.readAllBytes());

            reader.close();
            AetherTSL assembler = new AetherTSL(printCompiledScript);
            TSCode[] compile = assembler.compile(code);
            for (int i = 0; i < compile.length; i++) save(compile[i], file);
            return true;
        } catch (IOException ignored) {
            System.out.println("File not found!");
            return false;
        }
    }
    public static boolean compileWithAether(File file) {
        return compileWithAether(file, false);
    }

    public static void runCompiledFile(File file, int[] in,  int[] out) {
        TSCode ts = load(file);
        if (ts != null) {
            TSLVirtualMachine vm = new TSLVirtualMachine(ts);
            Scanner scanner = new Scanner(System.in);
            Locale.setDefault(Locale.ENGLISH);

            System.out.println("Enter values: ");
            for (int j = 0; j < in.length; j++) {
                if (in[j] < ts.varBuffSize)
                    vm.setVariable(in[j], scanner.nextFloat());
                else {
                    if (in[j] - ts.varBuffSize < ts.vec2BuffSize)
                        vm.setVariable(in[j],
                                new TSLVirtualMachine.Vector2(scanner.nextFloat(), scanner.nextFloat()));
                    else
                        vm.setVariable(in[j],
                                new TSLVirtualMachine.Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat()));
                }
            }
            System.out.println("Running...");

            vm.runCode();

            for (int i = 0; i < out.length; i++)
                System.out.print(vm.getVariable(out[i]) + " ");
        }
        else System.out.println("File not found!");
    }
    public static void testAssemblerCode(String code, int @NotNull [] in, int[] out) {
        TSLAssembler assembler = new TSLAssembler();
        TSCode ts = assembler.compile(code);
        TSLVirtualMachine vm = new TSLVirtualMachine(ts);
        Scanner scanner = new Scanner(System.in);
        Locale.setDefault(Locale.ENGLISH);

        System.out.println("Enter values: ");
        for (int j = 0; j < in.length; j++) {
            if (in[j] < ts.varBuffSize)
                vm.setVariable(in[j], scanner.nextFloat());
            else {
                if (in[j] - ts.varBuffSize < ts.vec2BuffSize)
                    vm.setVariable(in[j],
                            new TSLVirtualMachine.Vector2(scanner.nextFloat(), scanner.nextFloat()));
                else
                    vm.setVariable(in[j],
                            new TSLVirtualMachine.Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat()));
            }
        }
        System.out.println("Running...");

        vm.runCode();

        for (int i = 0; i < out.length; i++)
            System.out.print(vm.getVariable(out[i]) + " ");
    }
    public static void testAetherCode(String code, boolean printCompiledScript) {
        AetherTSL compiler = new AetherTSL(printCompiledScript);
        TSCode[] ts = compiler.compile(code);
        for (int i = 0; i < ts.length; i++) {
            TSLVirtualMachine vm = new TSLVirtualMachine(ts[i]);
            Scanner scanner = new Scanner(System.in);
            Locale.setDefault(Locale.ENGLISH);

            System.out.println("Enter values: ");
            for (int j = 0; j < ts[i].ioIndexes.length; j++) {
                if (ts[i].ioIndexes[j] < ts[i].varBuffSize)
                    vm.setVariable(ts[i].ioIndexes[j], scanner.nextFloat());
                else {
                    if (ts[i].ioIndexes[j] - ts[i].varBuffSize < ts[i].vec2BuffSize)
                        vm.setVariable(ts[i].ioIndexes[j],
                                new TSLVirtualMachine.Vector2(scanner.nextFloat(), scanner.nextFloat()));
                    else
                        vm.setVariable(ts[i].ioIndexes[j],
                                new TSLVirtualMachine.Vector3(scanner.nextFloat(), scanner.nextFloat(), scanner.nextFloat()));
                }
            }
            System.out.println("Running...");

            vm.runCode();

            for (int j = 0; j < ts[i].ioIndexes.length; j++)
                System.out.print(vm.getVariable(ts[i].ioIndexes[j]) + " ");
        }

    }
    public static void testAetherCode(String code) {
        testAetherCode(code, false);
    }
}
