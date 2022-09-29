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
import java.util.Objects;

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
//                    System.out.println(assemblerCode);

                    TSCode compile = assembler.compile(assemblerCode.toString());
                    if (function.name.equals("vtex")) compile.modulationKey = 4738;
                    else compile.modulationKey = 11738;


                    List<Integer> indexes = new ArrayList<>();
                    List<String> names = new ArrayList<>();
                    for (int i = 0; i < function.inputNames.length; i++) {
                        if (function.inputNames[i].contains("[")) {
                            int oBrI = function.inputNames[i].indexOf("[");
                            int cBrI = function.inputNames[i].indexOf("]");
                            if (cBrI > oBrI) {
                                int size = Integer.parseInt(function.inputNames[i].substring(oBrI + 1, cBrI));
                                String name = function.inputNames[i].substring(0, oBrI);
                                for (int j = 0; j < size; j++) {
                                    if (function.inputTypes[i].equals("vec3"))
                                        indexes.add(i + compile.varBuffSize + compile.vec2BuffSize + j);
                                    else if (function.inputTypes[i].equals("vec2"))
                                        indexes.add(i + compile.varBuffSize + j);
                                    else  indexes.add(i + j);
                                    names.add(name + j);
                                }
                            }
                            else throw new CompilationException("AetherTSL", "Incorrect array initialization", -1);
                        }
                        else {
                            if (function.inputTypes[i].equals("vec3"))
                                indexes.add(i + compile.varBuffSize + compile.vec2BuffSize);
                            else if (function.inputTypes[i].equals("vec2"))
                                indexes.add(i + compile.varBuffSize);
                            else  indexes.add(i);
                            names.add(function.inputNames[i]);
                        }
                    }
                    compile.ioIndexes = new int[indexes.size()];
                    compile.ioNames = new String[indexes.size()];
                    for (int i = 0; i < indexes.size(); i++) {
                        compile.ioNames[i] = names.get(i);
                        compile.ioIndexes[i] = indexes.get(i);
                    }

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

    public static @NotNull List<Token> lexicalAnalyser(@NotNull String code) {
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
        getSegmentToken(tokens, segment.toString(), line);

        return tokens;
    }

    private static boolean isArray = false;
    private static void getSegmentToken(List<Token> tokens, @NotNull String segment, int line) {
        if (isArray) {
            tokens.get(tokens.size() - 1).token += " " + segment;
            if (segment.contains("]")) isArray = false;
        }
        else switch (segment) {
            case "=", "+", "-", "*", "/", "%",
                    "+=", "-=", "*=", "/=", "%=",
                    "<", "<=", ">", ">=" -> tokens.add(new Token(segment, TokenType.Operator, line));
            case "while", "do", "for", "if", "kill", "def" -> tokens.add(new Token(segment, TokenType.ResWord, line));
            case "sin", "cos", "tan", "pow", "sqrt", "log", "abs", "reflect", "ray", "dot", "cross", "length", "intersect", "normalOf", "contain", "lerp" -> tokens.add(new Token(segment, TokenType.MathFunction, line));
            case "var", "vec2", "vec3" -> tokens.add(new Token(segment, TokenType.VariableMark, line));
            default -> {
                if (!segment.isBlank()) {
                    try {
                        double v = Double.parseDouble(segment);
                        tokens.add(new Token(String.valueOf(v), TokenType.Constant, line));
                    } catch (Exception ex) {
                        if (tokens.size() > 0 && tokens.get(tokens.size() - 1).token.equals("."))
                            tokens.add(new Token(segment, TokenType.ResWord, line));
                        else {
                            if (segment.endsWith("++") || segment.endsWith("--")) {
                                tokens.add(new Token(segment.substring(0, segment.length() - 2), TokenType.Variable, line));
                                tokens.add(new Token( segment.charAt(segment.length() - 1) + "=", TokenType.Operator, line));
                                tokens.add(new Token("1.0", TokenType.Constant, line));
                            }
                            else {
                                if (segment.contains("[") && !segment.contains("]")) isArray = true;
                                tokens.add(new Token(segment, TokenType.Variable, line));
                            }
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

                        if (!"{".equals(tokens.get(i + j + 1).token)) {
                            j++;
                            buffer.add(buffer.size() + s - k, new Token("{", TokenType.Bracket, tokens.get(i + j).line));
                            while (!";".equals(tokens.get(i + j).token)) {
                                buffer.add(buffer.size() + s - k, tokens.get(i + j ));
                                j++;
                            }
                            buffer.add(buffer.size() + s - k, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                            buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                            buffer.add(buffer.size() + s, new Token("}", TokenType.Bracket, tokens.get(i + j).line));
                        }
                        else {
                            buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                            buffer.add(buffer.size() + s, new Token("}", TokenType.Bracket, tokens.get(i + j).line));
                            shift.add(k + 2);
                            s -= shift.get(shift.size() - 1);
                        }
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
                    case "if" -> {
                        int j = 0;
                        while (!")".equals(tokens.get(i + j).token)) {
                            if (",".equals(tokens.get(i + j).token))
                                buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                            else buffer.add(buffer.size() + s, tokens.get(i + j));
                            j++;
                        }
                        buffer.add(buffer.size() + s, new Token(")", TokenType.Bracket, tokens.get(i + j).line));
                        j++;

                        if (!"{".equals(tokens.get(i + j).token)) {
                            buffer.add(buffer.size() + s, new Token("{", TokenType.Bracket, tokens.get(i + j).line));
                            while (!";".equals(tokens.get(i + j).token)) {
                                buffer.add(buffer.size() + s, tokens.get(i + j));
                                j++;
                            }
                            buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                            buffer.add(buffer.size() + s, new Token("}", TokenType.Bracket, tokens.get(i + j).line));
                        }
                        i += j;
                    }
                    case "else" -> {
                        buffer.add(buffer.size() + s, tokens.get(i));
                        int j = 1;
                        if (!"{".equals(tokens.get(i + j).token)) {
                            buffer.add(buffer.size() + s, new Token("{", TokenType.Bracket, tokens.get(i + j).line));
                            while (!";".equals(tokens.get(i + j).token)) {
                                buffer.add(buffer.size() + s, tokens.get(i + j ));
                                j++;
                            }
                            buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
                            buffer.add(buffer.size() + s, new Token("}", TokenType.Bracket, tokens.get(i + j).line));
                        }
                        i += j;
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
                    output.add(output.size() + s, new Token(buffer.get(i - 1).token, buffer.get(i - 1).type, buffer.get(i - 1).line));
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
                                tree.generateOpcode(varName, varType, output, s);

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
                                List<Token> indexTokens = lexicalAnalyser(index);
                                for (int k = 0; k < varName.size(); k++)
                                    if (varName.get(k).equals(name)) {
                                        name = varType.get(k) + "_" + name;
                                        break;
                                    }
                                for (Token indexToken : indexTokens)
                                    if (indexToken.type == TokenType.Variable) {
                                        for (int k = 0; k < varName.size(); k++)
                                            if (varName.get(k).equals(indexToken.token)) {
                                                indexToken.token = varType.get(k) + "_" + indexToken.token;
                                                break;
                                            }
                                    }
                                SyntaxTree tree = new SyntaxTree();
                                tree.buildTree(indexTokens);
                                tree.generateOpcode(varName, varType, output, s);

                                command = "seta " + name + " " + tree.token.token + " ";
                                for (int k = j + 1; k < line.size(); k++)
                                    if (line.get(k).type == TokenType.Variable)
                                        editVariable(line, varName, varType, k);

                                tree = new SyntaxTree();
                                tree.buildTree(line.subList(j + 1, line.size()));
                                tree.generateOpcode(varName, varType, output, s);
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
                        tree.generateOpcode(varName, varType, output, s);
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
                        tree.generateOpcode(varName, varType, output, s);
                        output.add(output.size() + s, "if " +  tree.token.token);
                    }
                    case "else" -> output.set(output.size() + s - 1, "else");
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
                            } else if (levelEnter != -1) {
                                assembler.remove(i);
                                assembler.add(levelEnter, line);
                                levelEnter++;
                            }
                            break;
                        }
                    }
                }
                case "set" -> {
                    if (levelEnter != -1 && (sgm[1].startsWith("num_") || sgm[1].startsWith("vec2_num") || sgm[1].startsWith("vec3_num"))) {
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

        for (int i = 0; i < assembler.size(); i++) {
            if (i < assembler.size() - 1) {
                String[] curr = assembler.get(i).split(" ");
                String[] next = assembler.get(i + 1).split(" ");
                if (curr.length > 2 && next.length == curr.length && curr[1].length() == next[1].length()) {
                    boolean eq = next[0].equals(curr[0]);
                    for (int j = 2; j < curr.length; j++)
                        if (!next[j].equals(curr[j])) {
                            eq = false;
                            break;
                        }

                    if (eq) {
                        int delta = 0;
                        for (int j = 0; j < curr[1].length(); j++)
                            if (next[1].charAt(j) != curr[1].charAt(j))
                                delta++;

                        if (delta == 1) {
                            assembler.remove(i + 1);
                            for (int j = i + 1; j < assembler.size(); j++) {
                                String[] s = assembler.get(j).split(" ");
                                StringBuilder command = new StringBuilder(s[0]);
                                for (int l = 1; l < s.length; l++) {
                                    if (s[l].equals(next[1])) command.append(" ").append(curr[1]);
                                    else command.append(" ").append(s[l]);
                                }
                                assembler.set(j, command.toString());
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
        for (int k = 0; k < varName.size(); k++) {
            if (line.get(j).token.contains("[")) {
                int oBrI = line.get(j).token.indexOf("[");
                int cBrI = line.get(j).token.indexOf("]");
                if (oBrI < cBrI) {
                    String name = line.get(j).token.substring(0, oBrI);
                    String index = line.get(j).token.substring(oBrI + 1, cBrI);
                    for (int l = 0; l < varName.size(); l++)
                        if (index.equals(varName.get(l)))
                            index = varType.get(l) + "_" + index;

                    line.get(j).token = varType.get(k) + "_" + name + "[" + index + "]";
                    break;
                }
                else throw new CompilationException("AetherTSL", "Incorrect array initialization", line.get(j).line);
            }
            else if (varName.get(k).equals(line.get(j).token)) {
                line.get(j).token = varType.get(k) + "_" + line.get(j).token;
                break;
            }
        }
    }

    public static Token generateVariable(int line) {
        numerator = (numerator + 1) % Long.MAX_VALUE;
        return new Token("v" + numerator, TokenType.Variable, line);
    }
}
