package ru.alexander.compilers.tsl.compilers;

import org.jetbrains.annotations.NotNull;
import ru.alexander.compilers.Compiler;
import ru.alexander.compilers.exception.CompilationException;
import ru.alexander.compilers.tsl.TSLVirtualMachine;
import ru.alexander.compilers.tsl.data.TSCode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TSLAssembler implements Compiler<TSCode> {
    @Override
    public TSCode compile(@NotNull String code) {
        ArrayList<String> opcode = new ArrayList<>(Arrays.asList(code.replace("\r", "").split("\n")));

        List<String> floatVars = new ArrayList<>();
        List<String> vec2Vars = new ArrayList<>();
        List<String> vec3Vars = new ArrayList<>();
        for (int i = 0; i < opcode.size(); i++) {
            String op = opcode.get(i);
            String[] sgm = op.split(" ");
            switch (sgm[0]) {
                case "var" -> {
                    testArray(floatVars, sgm[1], i);
                    opcode.remove(i);
                    i--;
                }
                case "vec2" -> {
                    testArray(vec2Vars, sgm[1], i);
                    opcode.remove(i);
                    i--;
                }
                case "vec3" -> {
                    testArray(vec3Vars, sgm[1], i);
                    opcode.remove(i);
                    i--;
                }
            }
        }

        List<Float> machineCode = new ArrayList<>();
        for (int j = 0; j < opcode.size(); j++) {
            String op = opcode.get(j);
            String[] sgm = op.split(" ");
            switch (sgm[0]) {
                case "set" -> {
                    machineCode.add(1f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    for (int i = 2; i < sgm.length; i++)
                        machineCode.add((float) Double.parseDouble(sgm[i]));
                }
                case "seta" -> {
                    machineCode.add(2f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1] + 0, j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "geta" -> {
                    machineCode.add(3f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2] + 0, j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "mov" -> {
                    machineCode.add(4f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }

                case "svx" -> {
                    machineCode.add(5f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "svy" -> {
                    machineCode.add(6f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "svz" -> {
                    machineCode.add(7f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }

                case "gvx" -> {
                    machineCode.add(8f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "gvy" -> {
                    machineCode.add(9f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "gvz" -> {
                    machineCode.add(10f);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }

                case "add" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 1));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "sub" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 2));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "mul" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 3));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "div" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 4));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "abs" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 5));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "pow" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 6));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "sqrt" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 7));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "log" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 8));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "sin" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 9));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "cos" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 10));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "tan" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 11));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "mt" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 12));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "met" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 13));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "lt" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 14));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "let" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 15));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "eq" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 16));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "arfl" -> {
                    machineCode.add((float) (TSLVirtualMachine.mathShift + 17));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }

                case "dot" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 1));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "crs" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 2));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "vlen" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 3));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                }
                case "itct" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 4));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[4], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[5], j);
                }
                case "vrfl" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 5));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "pinc" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 6));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[4], j);
                }
                case "cray" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 7));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "getn" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 8));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                }
                case "lerp" -> {
                    machineCode.add((float) (TSLVirtualMachine.vecMathShift + 9));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[2], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[3], j);
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[4], j);
                }

                case "end" -> machineCode.add((float) (TSLVirtualMachine.funcShift + 1));
                case "if" -> {
                    machineCode.add((float) (TSLVirtualMachine.funcShift + 2));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                }
                case "cyc" -> {
                    machineCode.add((float) (TSLVirtualMachine.funcShift + 3));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                }
                case "kill" -> {
                    machineCode.add((float) (TSLVirtualMachine.funcShift + 4));
                    useVar(floatVars, vec2Vars, vec3Vars, machineCode, sgm[1], j);
                }
            }
        }
        float[] codeRunner = new float[machineCode.size()];
        for (int i = 0; i < machineCode.size(); i++) codeRunner[i] = machineCode.get(i);

        TSCode output = new TSCode();
        output.varBuffSize = floatVars.size();
        output.vec2BuffSize = vec2Vars.size();
        output.vec3BuffSize = vec3Vars.size();
        output.script = codeRunner;
        return output;
    }

    private void testArray(List<String> variables, @NotNull String sgm, int line) {
        int oBrI = sgm.indexOf('[');
        int cBrI = sgm.indexOf(']');
        if (oBrI != -1 || cBrI != -1) {
            if (oBrI != -1 && oBrI < cBrI) {
                int size = Integer.parseInt(sgm.substring(oBrI + 1, cBrI));
                String name = sgm.substring(0, oBrI);
                for (int j = 0; j < size; j++) {
                    int finalJ = j;
                    if (variables.stream().noneMatch(v -> v.equals(name + finalJ))) variables.add(name + j);
                }
            } else throw new CompilationException("TSLAssembler", "variable array creation error", line);
        } else if (variables.stream().noneMatch(v -> v.equals(sgm))) variables.add(sgm);
    }

    private void useVar(@NotNull List<String> floatVars, List<String> vec2Vars, List<String> vec3Vars, List<Float> code, String sgm, int line) {
        boolean finded = false;
        for (int i = 0; i < floatVars.size(); i++) {
            if (floatVars.get(i).equals(sgm)) {
                code.add((float) i);
                finded = true;
                break;
            }
        }
        if (!finded) {
            for (int i = 0; i < vec2Vars.size(); i++) {
                if (vec2Vars.get(i).equals(sgm)) {
                    code.add((float) (i + floatVars.size()));
                    finded = true;
                    break;
                }
            }
            if (!finded) {
                for (int i = 0; i < vec3Vars.size(); i++) {
                    if (vec3Vars.get(i).equals(sgm)) {
                        code.add((float) (i + floatVars.size() + vec2Vars.size()));
                        finded = true;
                        break;
                    }
                }
                if (!finded)  throw new CompilationException("TSLAssembler", "variable not found: " + sgm, line);
            }
        }
    }
}
