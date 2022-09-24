package ru.alexander.compilers.tsl.compilers;

import org.jetbrains.annotations.NotNull;
import ru.alexander.compilers.exception.CompilationException;
import ru.alexander.compilers.tsl.TSLVirtualMachine;
import ru.alexander.compilers.tsl.data.TSCode;

import java.util.ArrayList;
import java.util.List;

public class TSLAssembler {

    private TSLAssembler() {}

    public static @NotNull TSCode builder(@NotNull List<String> opcode) {
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
                    vec2Vars.add(sgm[1]);
                    opcode.remove(i);
                    i--;
                }
                case "vec3" -> {
                    testArray(vec3Vars, sgm[1], i);
                    vec3Vars.add(sgm[1]);
                    opcode.remove(i);
                    i--;
                }
            }
        }

        List<Float> code = new ArrayList<>();
        for (String op : opcode) {
            String[] sgm = op.split(" ");
            switch (sgm[0]) {
                case "set" -> {
                    code.add(1f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    for (int i = 2; i < sgm.length; i++)
                        code.add((float) Double.parseDouble(sgm[i]));
                }
                case "seta" -> {
                    code.add(2f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    for (int i = 3; i < sgm.length; i++)
                        code.add((float) Double.parseDouble(sgm[i]));
                }
                case "mov" -> {
                    code.add(3f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }

                case "svx" -> {
                    code.add(4f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "svy" -> {
                    code.add(5f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "svz" -> {
                    code.add(6f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }

                case "gvx" -> {
                    code.add(7f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "gvy" -> {
                    code.add(8f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "gvz" -> {
                    code.add(9f);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }

                case "add" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 1));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "sub" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 2));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "mul" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 3));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "div" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 4));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "abs" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 5));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "pow" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 6));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "sqrt" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 7));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "log" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 8));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "sin" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 9));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "cos" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 10));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "tan" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 11));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "mt" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 12));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "met" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 13));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "lt" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 14));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "let" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 15));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "eq" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 16));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "arfl" -> {
                    code.add((float) (TSLVirtualMachine.mathShift + 17));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }

                case "dot" -> {
                    code.add((float) (TSLVirtualMachine.vecMathShift + 1));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "crs" -> {
                    code.add((float) (TSLVirtualMachine.vecMathShift + 2));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "vlen" -> {
                    code.add((float) (TSLVirtualMachine.vecMathShift + 3));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                }
                case "irct" -> {
                    code.add((float) (TSLVirtualMachine.vecMathShift + 4));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[4]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[5]);
                }
                case "vrfl" -> {
                    code.add((float) (TSLVirtualMachine.vecMathShift + 5));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }
                case "cray" -> {
                    code.add((float) (TSLVirtualMachine.vecMathShift + 6));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[2]);
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[3]);
                }

                case "end" -> code.add((float) (TSLVirtualMachine.funcShift + 1));
                case "if" -> {
                    code.add((float) (TSLVirtualMachine.funcShift + 2));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                }
                case "cyc" -> {
                    code.add((float) (TSLVirtualMachine.funcShift + 3));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                }
                case "kill" -> {
                    code.add((float) (TSLVirtualMachine.funcShift + 4));
                    useVar(floatVars, vec2Vars, vec3Vars, code, sgm[1]);
                }
            }
        }
        float[] codeRunner = new float[code.size()];
        for (int i = 0; i < code.size(); i++) codeRunner[i] = code.get(i);

        TSCode output = new TSCode();
        output.varBuffSize = floatVars.size();
        output.vec2BuffSize = vec2Vars.size();
        output.vec3BuffSize = vec3Vars.size();
        output.script = codeRunner;
        return output;
    }

    private static void testArray(List<String> variables, String sgm, int line) {
        int oBrI = sgm.indexOf('[');
        int cBrI = sgm.indexOf(']');
        if (oBrI != -1 || cBrI != -1) {
            if (oBrI != -1 && oBrI < cBrI) {
                int size = Integer.parseInt(sgm.substring(oBrI + 1, cBrI));
                for (int j = 0; j < size; j++) variables.add(sgm + j);
            } else throw new CompilationException("TSLAssembler", "variable array creation error", line);
        } else variables.add(sgm);
    }

    private static void useVar(@NotNull List<String> floatVars, List<String> vec2Vars, List<String> vec3Vars, List<Float> code, String sgm) {
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
                for (int i = 0; i < vec2Vars.size(); i++) {
                    if (vec3Vars.get(i).equals(sgm)) {
                        code.add((float) (i + floatVars.size() + vec2Vars.size()));
                        break;
                    }
                }
            }
        }
    }

}
