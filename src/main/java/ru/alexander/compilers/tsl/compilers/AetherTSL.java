package ru.alexander.compilers.tsl.compilers;

import org.jetbrains.annotations.NotNull;
import ru.alexander.compilers.Compiler;
import ru.alexander.compilers.exception.CompilationException;
import ru.alexander.compilers.tsl.data.SyntaxTree;
import ru.alexander.compilers.tsl.data.TSCode;
import ru.alexander.compilers.tsl.data.Function;
import ru.alexander.compilers.tsl.data.tokens.Token;
import ru.alexander.compilers.tsl.data.tokens.TokenType;

import java.util.ArrayList;
import java.util.List;

public class AetherTSL implements Compiler<TSCode[]> {
    private static long numerator = 0;

    private final boolean printCompiledScript;

    public AetherTSL(boolean printCompiledScript) {
        this.printCompiledScript = printCompiledScript;
    }

    public AetherTSL() {
        printCompiledScript = false;
    }

    @Override
    public TSCode[] compile(String code) {
        List<TSCode> scripts = new ArrayList<>();
        List<Token> tokens = lexicalAnalyser(code);
        List<Function> functions = functionSplitter(tokens);

        TSLAssembler assembler = new TSLAssembler();
        for (Function function : functions) {
            switch (function.name) {
                case "vtex", "litex" -> {
                    System.out.println("=================Compiling info=================");
                    System.out.println("Function name: " + function.name);

                    List<Token> composed = composer(tokens, function, functions);
                    composed = simplifier(composed);
                    for (int i = function.inputNames.length - 1; i >= 0; i--) {
                        composed.add(0, new Token(";", TokenType.Splitter, -1));
                        composed.add(0, new Token(function.inputNames[i], TokenType.Variable, -1));
                        composed.add(0, new Token(function.inputTypes[i], TokenType.VariableMark, -1));
                    }

                    List<String> opcode = opcodeGenerator(composed);

                    System.out.println("Unoptimised assembler code size: " + opcode.size());
                    double compressionRate = opcode.size();

                    optimiser(opcode);
                    System.out.println("Optimised assembler code size: " + opcode.size());
                    compressionRate = (1 - opcode.size() / compressionRate) * 100;

                    StringBuilder assemblerCode = new StringBuilder();
                    for (String command : opcode) assemblerCode.append(command).append("\n");
                    TSCode compile = assembler.compile(assemblerCode.toString());
                    if (function.name.equals("vtex")) compile.modulationKey = 4738;
                    else compile.modulationKey = 11738;
                    compile.ioIndexes = new int[function.inputNames.length];
                    for (int i = 0; i < compile.ioIndexes.length; i++) compile.ioIndexes[i] = i;

                    System.out.println("Assembler code compression: " + compressionRate + " %");
                    System.out.println("Float variables count: " + compile.varBuffSize);
                    System.out.println("Vector2 variables count: " + compile.vec2BuffSize);
                    System.out.println("Vector3 variables count: " + compile.vec3BuffSize);
                    System.out.println("Machine code size: " + compile.script.length);
                    if (printCompiledScript) {
                        System.out.println("---------------------Script---------------------");
                        System.out.println(assemblerCode);
                    }
                    System.out.println();
                    scripts.add(compile);
                }
            }
        }

        return scripts.toArray(new TSCode[0]);
    }

    private @NotNull List<Token> lexicalAnalyser(@NotNull String code) {
        int line = 0;
        String sc = code.replace("\n", " ").replace("\t", " ");
        List<Token> tokens = new ArrayList<>();

        StringBuilder segment = new StringBuilder();
        for (int i = 0; i < sc.length(); i++) {
            char c = sc.charAt(i);
            switch (c) {
                case ';' -> {
                    getSegmentToken(tokens, segment.toString(), line);
                    segment = new StringBuilder();

                    tokens.add(new Token(";", TokenType.Splitter, line));
                    line++;
                }
                case ',' -> {
                    getSegmentToken(tokens, segment.toString(), line);
                    segment = new StringBuilder();

                    tokens.add(new Token(",", TokenType.Splitter, line));
                }
                case '(' -> {
                    getSegmentToken(tokens, segment.toString(), line);
                    segment = new StringBuilder();

                    if (tokens.get(tokens.size() - 1).type == TokenType.Variable)
                        tokens.get(tokens.size() - 1).type = TokenType.Function;
                    tokens.add(new Token("(", TokenType.Bracket, line));
                }
                case ')' -> {
                    getSegmentToken(tokens, segment.toString(), line);
                    segment = new StringBuilder();

                    tokens.add(new Token(")", TokenType.Bracket, line));
                }
                case '{' -> {
                    getSegmentToken(tokens, segment.toString(), line);
                    segment = new StringBuilder();

                    tokens.add(new Token("{", TokenType.Bracket, line));
                }
                case '}' -> {
                    getSegmentToken(tokens, segment.toString(), line);
                    segment = new StringBuilder();

                    tokens.add(new Token("}", TokenType.Bracket, line));
                }
                case ' ' -> {
                    getSegmentToken(tokens, segment.toString(), line);
                    segment = new StringBuilder();
                }
                case '.' -> {
                    getSegmentToken(tokens, segment.toString(), line);
                    segment = new StringBuilder();
                    tokens.add(new Token(".", TokenType.Splitter, line));
                }

                default -> {
                    if (c == '[' && tokens.get(tokens.size() - 1).type == TokenType.Operator)
                        tokens.add(new Token("" + c, TokenType.Bracket, line));
                    else if (c == ']' && tokens.get(tokens.size() - 1).token.equals(",")) {
                        getSegmentToken(tokens, segment.toString(), line);
                        segment = new StringBuilder();

                        tokens.add(new Token("" + c, TokenType.Bracket, line));
                    }
                    else segment.append(c);
                }
            }
        }

        return tokens;
    }
    private void getSegmentToken(List<Token> tokens, @NotNull String segment, int line) {
        switch (segment) {
            case "=", "+", "-", "*", "/", "%",
                    "+=", "-=", "*=", "/=", "%=",
                    "<", "<=", ">", ">=" -> tokens.add(new Token(segment, TokenType.Operator, line));
            case "while", "do", "for", "if", "kill", "def" -> tokens.add(new Token(segment, TokenType.ResWord, line));
            case "sin", "cos", "tan", "pow", "sqrt", "log", "abs", "reflect", "ray", "dot", "cross", "length", "intersect", "normal", "contain", "lerp" -> tokens.add(new Token(segment, TokenType.MathFunction, line));
            case "var", "vec2", "vec3" -> tokens.add(new Token(segment, TokenType.VariableMark, line));
            default -> {
                if (!segment.isBlank()) {
                    try {
                        double v = Double.parseDouble(segment);
                        tokens.add(new Token(String.valueOf(v), TokenType.Constant, line));
                    } catch (Exception ex) {
                        if (tokens.get(tokens.size() - 1).token.equals("."))
                            tokens.add(new Token(segment, TokenType.ResWord, line));
                        else {
                            if (segment.endsWith("++") || segment.endsWith("--")) {
                                tokens.add(new Token(segment.substring(0, segment.length() - 2), TokenType.Variable, line));
                                tokens.add(new Token( segment.charAt(segment.length() - 1) + "=", TokenType.Operator, line));
                                tokens.add(new Token("1.0", TokenType.Constant, line));
                            }
                            else tokens.add(new Token(segment, TokenType.Variable, line));
                        }
                    }
                }
            }
        }
    }

    private @NotNull List<Function> functionSplitter(@NotNull List<Token> tokens) {
        List<Function> output = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).token.equals("def")) {
                Function function = new Function();
                function.name = tokens.get(i + 1).token;
                List<String> par = new ArrayList<>();
                List<String> parTypes = new ArrayList<>();
                int j = i + 5;
                while (!tokens.get(j).token.equals(")")) {
                    par.add(tokens.get(j - 1).token);
                    parTypes.add(tokens.get(j - 2).token);
                    j += 3;
                }
                par.add(tokens.get(j - 1).token);
                parTypes.add(tokens.get(j - 2).token);
                j++;

                function.inputNames = par.toArray(new String[0]);
                function.inputTypes = parTypes.toArray(new String[0]);
                function.codeStart = j + 1;
                j++;
                int level = 1;
                while (level != 0) {
                    if (tokens.get(j).token.equals("{")) level++;
                    if (tokens.get(j).token.equals("}")) level--;
                    j++;
                }
                function.codeEnd = j - 1;
                output.add(function);
            }
        }

        return output;
    }

    private int recursionDepth = 0;
    private @NotNull List<Token> composer(List<Token> tokens, @NotNull Function main, List<Function> functions) {
        List<Token> output = new ArrayList<>();
        List<String> varName = new ArrayList<>();
        List<String> varType = new ArrayList<>();

        for (int i = main.codeStart; i < main.codeEnd; i++) {
            Token token = tokens.get(i);
            if (token.type == TokenType.Function) {
                List<Function> fs = functions.stream().filter(f -> f.name.equals(token.token)).toList();
                if (fs.size() == 0) throw new CompilationException("AetherTSL", "function not exists", token.line);
                else {
                    List<String> par = new ArrayList<>();
                    int j = i + 2;
                    while (!tokens.get(j).token.equals(")")) {
                        par.add(tokens.get(j).token);
                        j++;
                    }
                    if (recursionDepth < 100) {

                        List<Function> function = fs.stream().filter(f -> f.inputNames.length == par.size()).toList();
                        if (function.size() == 0) throw new CompilationException("AetherTSL", "function not exists", token.line);
                        else if (function.size() == 1) {
                            for (int l = 0; l < par.size(); l++) {
                                boolean correct = false;
                                for (int k = 0; k < varName.size(); k++)
                                    if (varName.get(k).equals(par.get(l))) {
                                        if (function.get(0).inputTypes[l].equals(varType.get(k))) correct = true;
                                        break;
                                    }
                                if (!correct) throw new CompilationException("AetherTSL", "used incorrect variable type", token.line);
                            }

                            recursionDepth++;
                            List<Token> compose = composer(tokens, function.get(0), functions);
                            for (Token tkn : compose) {
                                if (tkn.type == TokenType.Variable) {
                                    for (int v = 0; v < function.get(0).inputNames.length; v++)
                                        if (function.get(0).inputNames[v].equals(tkn.token))
                                            tkn.token = par.get(v);
                                }

                                output.add(tkn);
                            }
                            recursionDepth--;
                        } else throw new CompilationException("AetherTSL", "multiple signatures exists", token.line);
                    } else {
                        int line = output.get(output.size() - 1).line + 1;
                        output.add(new Token("kill", TokenType.ResWord, line));
                        output.add(new Token("773", TokenType.Constant, line));
                        output.add(new Token(";", TokenType.Splitter, line));
                    }
                    i = j + 1;
                }

            } else {
                if (token.type == TokenType.VariableMark) {
                    varType.add(token.token);
                    varName.add(tokens.get(i + 1).token);
                }
                output.add(token);
            }
        }

        return output;
    }

    private @NotNull List<Token> simplifier(@NotNull List<Token> tokens) {
            List<Token> buffer = new ArrayList<>();
            List<Integer> shift = new ArrayList<>();
            int s = 0;
            for (int i = 0; i < tokens.size(); i++) {
                switch (tokens.get(i).token) {
                    case "for" -> {
                        int j = 0;
                        while (!"(".equals(tokens.get(i + j).token)) j++;
                        j++;

                        while (!";".equals(tokens.get(i + j).token)) {
                            if (",".equals(tokens.get(i + j).token))
                                buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                            else buffer.add(buffer.size() + s, tokens.get(i + j));
                            j++;
                        }
                        j++;
                        buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));

                        buffer.add(buffer.size() + s, new Token("while", TokenType.ResWord, tokens.get(i + j).line));
                        buffer.add(buffer.size() + s, new Token("(", TokenType.Bracket, tokens.get(i + j).line));
                        while (!";".equals(tokens.get(i + j).token)) {
                            buffer.add(buffer.size() + s, tokens.get(i + j));
                            j++;
                        }
                        j++;
                        buffer.add(buffer.size() + s, new Token(")", TokenType.Bracket, tokens.get(i + j).line));

                        int k = 0;
                        while (!")".equals(tokens.get(i + j).token)) {
                            if (",".equals(tokens.get(i + j).token))
                                buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                            else buffer.add(buffer.size() + s, tokens.get(i + j));
                            j++;
                            k++;
                        }
                        buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                        buffer.add(buffer.size() + s, new Token("}", TokenType.Bracket, tokens.get(i + j).line));
                        shift.add(k + 2);
                        s -= shift.get(shift.size() - 1);
                        i += j;
                    }
                    case "do" -> {
                        int j = 2;
                        List<Token> inBr = new ArrayList<>();
                        while (!tokens.get(i + j).token.equals("}")) {
                            inBr.add(tokens.get(i + j));
                            j++;
                        }
                        i += j + 1;
                        buffer.addAll(inBr);
                        j = 0;
                        while (!tokens.get(i + j).token.equals(";")) {
                            buffer.add(tokens.get(i + j));
                            j++;
                        }
                        buffer.add(new Token("{", TokenType.Bracket, tokens.get(i).line));
                        buffer.addAll(inBr);
                        buffer.add(new Token("}", TokenType.Bracket, tokens.get(i).line));

                    }
                    case "}" -> {
                        if (shift.size() > 0) s += shift.remove(shift.size() - 1);
                        else {
                            buffer.add(buffer.size(), tokens.get(i));
                        }
                    }
                    default -> buffer.add(buffer.size() + s, tokens.get(i));
                }
            }

            s = 0;
            List<Token> output = new ArrayList<>();
            for (int i = 0; i < buffer.size(); i++) {
                if ("+=".equals(buffer.get(i).token)
                        || "-=".equals(buffer.get(i).token)
                        || "*=".equals(buffer.get(i).token)
                        || "/=".equals(buffer.get(i).token)
                        || "%=".equals(buffer.get(i).token)) {

                    output.add(output.size() + s, new Token("=", TokenType.Operator, buffer.get(i).line));
                    if (buffer.get(i - 2).token.equals(".")) {
                        output.add(output.size() + s, buffer.get(i - 3));
                        output.add(output.size() + s, buffer.get(i - 2));
                    }
                    output.add(output.size() + s, buffer.get(i - 1));
                    output.add(output.size() + s, new Token(buffer.get(i).token.replace("=", ""), TokenType.Operator, buffer.get(i).line));
                    output.add(output.size() + s, new Token("(", TokenType.Bracket, buffer.get(i).line));
                    output.add(output.size() + s, new Token(")", TokenType.Bracket, buffer.get(i).line));
                    s--;
                } else {
                    if (";".equals(buffer.get(i).token)) s = Math.min(s + 1, 0);
                    output.add(output.size() + s, buffer.get(i));
                }
            }

            return output;
        }

    private static final String levelSymbol = " ";
    private @NotNull List<String> opcodeGenerator(@NotNull List<Token> tokens) {
        List<List<Token>> lines = new ArrayList<>();
        List<Token> line = new ArrayList<>();
        int layer = 0;
        for (Token token : tokens) {
            switch (token.token) {
                case ";" -> {
                    lines.add(line);
                    line = new ArrayList<>();
                    for (int i = 0; i < layer; i++) line.add(new Token(levelSymbol, TokenType.Splitter, token.line));
                }
                case "{" -> {
                    layer++;
                    lines.add(line);
                    line = new ArrayList<>();
                    for (int i = 0; i < layer; i++) line.add(new Token(levelSymbol, TokenType.Splitter, token.line));
                }
                case "}" -> {
                    layer--;
                    if (line.size() != 0 && line.size() != layer + 1) lines.add(line);
                    line = new ArrayList<>();
                    for (int i = 0; i < layer; i++) line.add(new Token(levelSymbol, TokenType.Splitter, token.line));
                }
                default -> line.add(token);
            }
        }

        List<Integer> shift = new ArrayList<>();
        int s = 0;

        List<String> varName = new ArrayList<>();
        List<String> varType = new ArrayList<>();

        int lvl;
        int prevLvl = 0;
        List<String> output = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            lvl = 0;
            line = lines.get(i);
            while (line.get(lvl).token.equals(levelSymbol)) lvl++;
            for (int j = lvl; j < prevLvl; j++) {
                if (shift.size() > 0) s += shift.remove(shift.size() - 1);
                output.add(output.size() - s, "end");
            }
            prevLvl = lvl;


            if (line.get(lvl).type == TokenType.VariableMark) {
                varType.add(line.get(lvl).token);
                if (line.get(lvl + 1).token.contains("["))
                    varName.add(line.get(lvl + 1).token.substring(0, line.get(lvl + 1).token.indexOf("[")));
                else varName.add(line.get(lvl + 1).token);
                output.add(output.size() + s, line.get(lvl).token + " " + line.get(lvl).token + "_" + line.get(lvl + 1).token);
            }

            for (int j = lvl; j < line.size(); j++) {
                switch (line.get(j).token) {
                    case "=" -> {
                        String command;
                        int oBrI = line.get(j - 1).token.indexOf("[");
                        int cBrI = line.get(j - 1).token.indexOf("]");
                        if (oBrI == -1 && cBrI == -1) {
                            for (int k = j + 1; k < line.size(); k++)
                                if (line.get(k).type == TokenType.Variable)
                                    editVariable(line, varName, varType, k);

                            if (j + 2 == line.size() && line.get(j + 1).type == TokenType.Variable) {
                                editVariable(line, varName, varType, j - 1);
                                output.add(output.size() + s, "mov " + line.get(j - 1).token + " " + line.get(j + 1).token);
                            }
                            else {

                                SyntaxTree tree = new SyntaxTree();
                                tree.buildTree(line.subList(j + 1, line.size()));
                                tree.generateOpcode(output, s);

                                if (j >= 2 && line.get(j - 2).token.equals(".")) {
                                    editVariable(line, varName, varType, j - 3);
                                    output.add(output.size() + s, "sv" + line.get(j - 1).token + " " + line.get(j - 3).token + " " + tree.token.token);
                                } else {
                                    editVariable(line, varName, varType, j - 1);

                                    String str = output.get(output.size() + s - 1);
                                    str = str.replace(tree.token.token, line.get(j - 1).token);
                                    output.set(output.size() + s - 1, str);
                                    if (output.get(output.size() + s - 2).contains(tree.token.token))
                                        output.remove(output.size() + s - 2);
                                }
                            }
                        }
                        else {
                            if (oBrI != -1 && oBrI < cBrI) {
                                String name = line.get(j - 1).token.substring(0, oBrI);
                                String index = line.get(j - 1).token.substring(oBrI + 1, cBrI);
                                for (int k = 0; k < varName.size(); k++)
                                    if (varName.get(k).equals(name)) {
                                        name = varType.get(k) + "_" + name;
                                        break;
                                    }
                                for (int k = 0; k < varName.size(); k++)
                                    if (varName.get(k).equals(index)) {
                                        index = varType.get(k) + "_" + index;
                                        break;
                                    }

                                command = "seta " + name + " " + index + " ";
                                for (int k = j + 1; k < line.size(); k++)
                                    if (line.get(k).type == TokenType.Variable)
                                        editVariable(line, varName, varType, k);

                                SyntaxTree tree = new SyntaxTree();
                                tree.buildTree(line.subList(j + 1, line.size()));
                                tree.generateOpcode(output, s);
                                output.add(output.size() + s, command + tree.token.token);

                            }
                            else throw new CompilationException("AetherTSL", "incorrect variable definition", line.get(j - 1).line);
                        }
                    }
                    case "while" -> {
                        for (int k = j + 2; k < line.size() - 1; k++)
                            if (line.get(k).type == TokenType.Variable)
                                editVariable(line, varName, varType, k);

                        SyntaxTree tree = new SyntaxTree();
                        int start = output.size();
                        tree.buildTree(line.subList(j + 2, line.size() - 1));
                        tree.generateOpcode(output, s);
                        int end = output.size();
                        output.add(output.size() + s, "cyc " +  tree.token.token);
                        output.addAll(output.subList(start, end));
                        shift.add(end - start);
                        s -= shift.get(shift.size() - 1);

                    }
                    case "if" -> {
                        for (int k = j + 2; k < line.size() - 1; k++)
                            if (line.get(k).type == TokenType.Variable)
                                editVariable(line, varName, varType, k);

                        SyntaxTree tree = new SyntaxTree();
                        tree.buildTree(line.subList(j + 2, line.size() - 1));
                        tree.generateOpcode(output, s);
                        output.add(output.size() + s, "if " +  tree.token.token);
                    }
                    case "kill" -> {
                        String var = "num_" + Double.doubleToLongBits(Double.parseDouble(line.get(j + 1).token));
                        output.add(output.size() + s, "var " + var);
                        output.add(output.size() + s, "set " + var + " " + line.get(j + 1).token);
                        output.add(output.size() + s, "kll " + var);
                    }
                }
            }
        }

        for (int j = 0; j < prevLvl; j++) {
            if (shift.size() > 0) s += shift.remove(shift.size() - 1);
            output.add(output.size() - s, "end");
        }
        return output;
    }


    private void optimiser(@NotNull List<String> assembler) {
        List<Integer> varFI = new ArrayList<>();
        List<String> varName = new ArrayList<>();
        for (int i = 0; i < assembler.size(); i++) {
            String[] sgm = assembler.get(i).split(" ");
            if ((sgm[0].equals("var") || sgm[0].equals("vec2") || sgm[0].equals("vec3"))
                    && varName.stream().noneMatch(s -> s.equals(sgm[1]))) {
                varFI.add(i);
                varName.add(sgm[1]);
            }
        }
        int levelEnter = -1;
        for (int i = 0; i < assembler.size(); i++) {
            String line = assembler.get(i);
            String[] sgm = line.split(" ");
            switch (sgm[0]) {
                case "cyc", "if" -> {
                    if (levelEnter == -1) levelEnter = i - 2;
                    else levelEnter = Math.min(levelEnter, i - 2);
                }
                case "end" -> levelEnter = -1;
                case "var", "vec2", "vec3" -> {
                    for (int j = 0; j < varName.size(); j++) {
                        if (varName.get(j).equals(sgm[1])) {
                            if (varFI.get(j) != i) {
                                i = recalculateFirstInclude(assembler, varFI, i);
                            }
                            else if (levelEnter != -1) {
                                assembler.remove(i);
                                assembler.add(levelEnter, line);
                                levelEnter++;
                            }
                            break;
                        }
                    }
                }
                case "set" -> {
                    if (levelEnter != -1 && (sgm[1].startsWith("num_") || sgm[1].startsWith("vec2_")|| sgm[1].startsWith("vec3_"))) {
                        for (int j = 0; j < varName.size(); j++) {
                            if (varName.get(j).equals(sgm[1])) {
                                if (varFI.get(j) + 1 != i) {
                                    i = recalculateFirstInclude(assembler, varFI, i);
                                } else {
                                    assembler.remove(i);
                                    assembler.add(levelEnter, line);
                                    levelEnter++;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    private int recalculateFirstInclude(@NotNull List<String> assembler, @NotNull List<Integer> varFI, int i) {
        for (int l = varFI.size() - 1; l >= 0; l--) {
            if (varFI.get(l) > i) varFI.set(l, varFI.get(l) - 1);
            else break;
        }

        assembler.remove(i);
        i--;
        return i;
    }

    private void editVariable(List<Token> line, @NotNull List<String> varName, List<String> varType, int j) {
        for (int k = 0; k < varName.size(); k++)
            if (varName.get(k).equals(line.get(j).token)) {
                if (line.get(j).token.contains("["))
                    line.get(j).token = varType.get(k) + "_" + line.get(j).token.substring(0,  line.get(j).token.indexOf("["));
                else line.get(j).token = varType.get(k) + "_" + line.get(j).token;
                break;
            }
    }

    public static Token generateVariable(int line) {
        numerator = (numerator + 1) % Long.MAX_VALUE;
        return new Token("v" + numerator, TokenType.Variable, line);
    }
}
