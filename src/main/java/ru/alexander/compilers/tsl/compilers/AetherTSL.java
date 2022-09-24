package ru.alexander.compilers.tsl.compilers;

import ru.alexander.compilers.tsl.data.tokens.Token;
import ru.alexander.compilers.tsl.data.tokens.TokenType;

public class AetherTSL {
    private static long numerator = 0;

//    public TSCode[] compile(@NotNull String code) {
//        List<TSCode> scripts = new ArrayList<>();
//
//        List<Token> tokens = lexicalAnalyser(code);
//        List<Function> functions = functionSplitter(tokens);
//
//        for (Function function : functions) {
//            switch (function.name) {
//                case "vtex", "litex" -> {
//                    System.out.println("=================Compiling info=================");
//                    System.out.println("Function name: " + function.name);
//
//                    List<Token> composed = composer(tokens, function, functions);
//                    composed = simplifier(composed);
//                    List<String> opcode = opcodeGenerator(composed);
//                    List<Variable> variables = registrator(opcode);
//                    double compressionRate = opcode.size();
//
//                    for (int i = 0; i < function.input.length; i++) {
//                        if (!function.input[i].equals(",")) {
//                            int finalI = i;
//                            if (variables.stream().noneMatch(v -> v.cipher.equals(function.input[finalI]))) {
//                                int oBrI = function.input[i].indexOf("[");
//                                int cBrI = function.input[i].indexOf("]");
//                                if (oBrI != -1) {
//                                    if (oBrI < cBrI) {
//                                        opcode.add(0, "arr " + function.input[i].substring(0, oBrI) + " " + function.input[i].substring(oBrI + 1, cBrI));
//                                        function.input[i] = function.input[i].substring(0, oBrI);
//                                    } else throw new CompilationException(-1, "incorrect variable definition");
//                                }
//                                else {
//                                    Variable variable = new Variable(function.input[i]);
//                                    variables.add(variable);
//                                }
//                            }
//                        }
//                    }
//
//                    optimiser(opcode, variables);
//                    variables = registrator(opcode);
//                    compressionRate = (1 - opcode.size() / compressionRate) * 100;
//                    System.out.println("Size: " + opcode.size());
//                    System.out.println("Variable count: " + variables.size());
//                    System.out.println("Compression rate: " + compressionRate + " %");
//
//                    List<Variable> inputRegister = new ArrayList<>();
//                    for (int i = 0; i < function.input.length; i++) {
//                        if (!function.input[i].equals(",")) {
//                            int finalI = i;
//                            Variable variable = variables.stream().filter(v -> v.cipher.equals(function.input[finalI])).findAny().orElse(null);
//                            if (variable == null) {
//                                variable = new Variable(function.input[i]);
//                                variables.add(variable);
//                            }
//                            else {
//                                variable.creationCall = -1;
//                                variable.lastCall = Integer.MAX_VALUE;
//                            }
//                            inputRegister.add(variable);
//                        }
//                    }
//
//                    TSCode ts = builder(opcode, variables);
//                    if (function.name.equals("vtex")) ts.modulationKey = 4738;
//                    else if (function.name.equals("litex")) ts.modulationKey = 11738;
//                    System.out.println("Variable buffer size: " + ts.varBuffSize);
//                    System.out.println("Script size: " + ts.script.length);
//                    System.out.println();
//
//                    ts.ioIndexes = new int[inputRegister.size()];
//                    for (int i = 0; i < inputRegister.size(); i++)
//                        ts.ioIndexes[i] = inputRegister.get(i).layerIndex;
//
//                    scripts.add(ts);
//                }
//            }
//        }
//
//        return scripts.toArray(new TSCode[0]);
//    }
//
//
//    private @NotNull List<Token> lexicalAnalyser(@NotNull String code) {
//        int line = 0;
//        String sc = code.replace("\n", " ").replace("\t", " ");
//        List<Token> tokens = new ArrayList<>();
//
//        StringBuilder segment = new StringBuilder();
//        for (int i = 0; i < sc.length(); i++) {
//            char c = sc.charAt(i);
//                switch (c) {
//                    case ';' -> {
//                        getSegmentToken(tokens, segment.toString(), line);
//                        segment = new StringBuilder();
//
//                        tokens.add(new Token(";", TokenType.Splitter, line));
//                        line++;
//                    }
//                    case ',' -> {
//                        getSegmentToken(tokens, segment.toString(), line);
//                        segment = new StringBuilder();
//
//                        tokens.add(new Token(",", TokenType.Splitter, line));
//                    }
//                    case '(' -> {
//                        getSegmentToken(tokens, segment.toString(), line);
//                        segment = new StringBuilder();
//
//                        if (tokens.get(tokens.size() - 1).type == TokenType.Variable)
//                            tokens.get(tokens.size() - 1).type = TokenType.Function;
//                        tokens.add(new Token("(", TokenType.Bracket, line));
//                    }
//                    case ')' -> {
//                        getSegmentToken(tokens, segment.toString(), line);
//                        segment = new StringBuilder();
//
//                        tokens.add(new Token(")", TokenType.Bracket, line));
//                    }
//                    case '{' -> {
//                        getSegmentToken(tokens, segment.toString(), line);
//                        segment = new StringBuilder();
//
//                        tokens.add(new Token("{", TokenType.Bracket, line));
//                    }
//                    case '}' -> {
//                        getSegmentToken(tokens, segment.toString(), line);
//                        segment = new StringBuilder();
//
//                        tokens.add(new Token("}", TokenType.Bracket, line));
//                    }
//                    case ' ' -> {
//                        getSegmentToken(tokens, segment.toString(), line);
//                        segment = new StringBuilder();
//                    }
//
//                    default -> segment.append(c);
//                }
//        }
//
//        return tokens;
//    }
//    private void getSegmentToken(List<Token> tokens, @NotNull String segment, int line) {
//        switch (segment) {
//            case "=", "+", "-", "*", "/", "%",
//                    "+=", "-=", "*=", "/=", "%=",
//                    "<", "<=", ">", ">=" -> tokens.add(new Token(segment, TokenType.Operator, line));
//            case "while", "do", "for", "if", "kill", "def", "var", "vec" -> tokens.add(new Token(segment, TokenType.ResWord, line));
//            case "sin", "cos", "tan", "pow", "log", "abs", "reflect", "vectorize" -> tokens.add(new Token(segment, TokenType.MathFunction, line));
//            default -> {
//                if (!segment.isBlank()) {
//                    try {
//                        double v = Double.parseDouble(segment);
//                        tokens.add(new Token(String.valueOf(v), TokenType.Constant, line));
//                    } catch (Exception ex) {
//                        tokens.add(new Token(segment, TokenType.Variable, line));
//                    }
//                }
//            }
//        }
//    }
//
//
//    private @NotNull List<Function> functionSplitter(@NotNull List<Token> tokens) {
//        List<Function> output = new ArrayList<>();
//        for (int i = 0; i < tokens.size(); i++) {
//            if (tokens.get(i).token.equals("def")) {
//                Function function = new Function();
//                function.name = tokens.get(i + 1).token;
//                List<String> par = new ArrayList<>();
//                int j = i + 3;
//                while (!tokens.get(j).token.equals(")")) {
//                    par.add(tokens.get(j).token);
//                    j++;
//                }
//                j++;
//
//                function.input = par.toArray(new String[0]);
//                function.codeStart = j + 1;
//                j++;
//                int level = 1;
//                while (level != 0) {
//                    if (tokens.get(j).token.equals("{")) level++;
//                    if (tokens.get(j).token.equals("}")) level--;
//                    j++;
//                }
//                function.codeEnd = j;
//                output.add(function);
//            }
//        }
//
//        return output;
//    }
//
//    private int recursionDepth = 0;
//    private @NotNull List<Token> composer(List<Token> tokens, @NotNull Function main, List<Function> functions) {
//        List<Token> output = new ArrayList<>();
//
//        for (int i = main.codeStart; i < main.codeEnd; i++) {
//            Token token = tokens.get(i);
//            if (token.type == TokenType.Function) {
//                List<Function> fs = functions.stream().filter(f -> f.name.equals(token.token)).toList();
//                if (fs.size() == 0) throw new CompilationException(token.line, "function not exists");
//                else {
//                    List<String> par = new ArrayList<>();
//                    int j = i + 2;
//                    while (!tokens.get(j).token.equals(")")) {
//                        par.add(tokens.get(j).token);
//                        j++;
//                    }
//                    if (recursionDepth < 100) {
//
//                        List<Function> function = fs.stream().filter(f -> f.input.length == par.size()).toList();
//                        if (function.size() == 0) throw new CompilationException(token.line, "function not exists");
//                        else if (function.size() == 1) {
//                            recursionDepth++;
//                            List<Token> compose = composer(tokens, function.get(0), functions);
//                            for (Token tkn : compose) {
//                                if (tkn.type == TokenType.Variable) {
//                                    for (int v = 0; v < function.get(0).input.length; v++)
//                                        if (function.get(0).input[v].equals(tkn.token))
//                                            tkn.token = par.get(v);
//                                }
//
//                                output.add(tkn);
//                            }
//                            recursionDepth--;
//                        } else throw new CompilationException(token.line, "multiple signatures exists");
//                    } else {
//                        int line = output.get(output.size() - 1).line + 1;
//                        output.add(new Token("kill", TokenType.ResWord, line));
//                        output.add(new Token("773", TokenType.Constant, line));
//                        output.add(new Token(";", TokenType.Splitter, line));
//                    }
//                    i = j + 1;
//                }
//
//            } else output.add(token);
//        }
//
//        return output;
//    }
//
//    private @NotNull List<Token> simplifier(@NotNull List<Token> tokens) {
//        List<Token> buffer = new ArrayList<>();
//        List<Integer> shift = new ArrayList<>();
//        int s = 0;
//        for (int i = 0; i < tokens.size(); i++) {
//            switch (tokens.get(i).token) {
//                case "for" -> {
//                    int j = 0;
//                    while (!"(".equals(tokens.get(i + j).token)) j++;
//                    j++;
//
//                    while (!";".equals(tokens.get(i + j).token)) {
//                        if (",".equals(tokens.get(i + j).token))
//                            buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
//                        else buffer.add(buffer.size() + s, tokens.get(i + j));
//                        j++;
//                    }
//                    j++;
//                    buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
//
//                    buffer.add(buffer.size() + s, new Token("while", TokenType.ResWord, tokens.get(i + j).line));
//                    buffer.add(buffer.size() + s, new Token("(", TokenType.Bracket, tokens.get(i + j).line));
//                    while (!";".equals(tokens.get(i + j).token)) {
//                        buffer.add(buffer.size() + s, tokens.get(i + j));
//                        j++;
//                    }
//                    j++;
//                    buffer.add(buffer.size() + s, new Token(")", TokenType.Bracket, tokens.get(i + j).line));
//
//                    int k = 0;
//                    while (!")".equals(tokens.get(i + j).token)) {
//                        if (",".equals(tokens.get(i + j).token))
//                            buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
//                        else buffer.add(buffer.size() + s, tokens.get(i + j));
//                        j++;
//                        k++;
//                    }
//                    buffer.add(buffer.size() + s, new Token(";", TokenType.Splitter, tokens.get(i + j).line));
//                    buffer.add(buffer.size() + s, new Token("}", TokenType.Bracket, tokens.get(i + j).line));
//                    shift.add(k + 2);
//                    s -= shift.get(shift.size() - 1);
//                    i += j;
//                }
//                case "do" -> {
//                    int j = 2;
//                    List<Token> inBr = new ArrayList<>();
//                    while (!tokens.get(i + j).token.equals("}")) {
//                        inBr.add(tokens.get(i + j));
//                        j++;
//                    }
//                    i += j + 1;
//                    buffer.addAll(inBr);
//                    j = 0;
//                    while (!tokens.get(i + j).token.equals(";")) {
//                        buffer.add(tokens.get(i + j));
//                        j++;
//                    }
//                    buffer.add(new Token("{", TokenType.Bracket, tokens.get(i).line));
//                    buffer.addAll(inBr);
//                    buffer.add(new Token("}", TokenType.Bracket, tokens.get(i).line));
//
//                }
//                case "}" -> {
//                    if (shift.size() > 0) s += shift.remove(shift.size() - 1);
//                    else {
//                        buffer.add(buffer.size(), tokens.get(i));
//                    }
//                }
//                default -> buffer.add(buffer.size() + s, tokens.get(i));
//            }
//        }
//
//        s = 0;
//        List<Token> output = new ArrayList<>();
//        for (int i = 0; i < buffer.size(); i++) {
//            if ("+=".equals(buffer.get(i).token)
//                    || "-=".equals(buffer.get(i).token)
//                    || "*=".equals(buffer.get(i).token)
//                    || "/=".equals(buffer.get(i).token)
//                    || "%=".equals(buffer.get(i).token)) {
//                Token var = buffer.get(i - 1);
//                output.add(output.size() + s, new Token("=", TokenType.Operator, buffer.get(i).line));
//                output.add(output.size() + s, var);
//                output.add(output.size() + s, new Token(buffer.get(i).token.replace("=", ""), TokenType.Operator, buffer.get(i).line));
//                output.add(output.size() + s, new Token("(", TokenType.Bracket, buffer.get(i).line));
//                output.add(output.size() + s, new Token(")", TokenType.Bracket, buffer.get(i).line));
//                s--;
//            } else {
//                if (";".equals(buffer.get(i).token)) s = Math.min(s + 1, 0);
//                output.add(output.size() + s, buffer.get(i));
//            }
//        }
//
//        return output;
//    }
//
//    private static final String levelSymbol = " ";
//    private @NotNull List<String> opcodeGenerator(@NotNull List<Token> tokens) {
//        List<List<Token>> lines = new ArrayList<>();
//        List<Token> line = new ArrayList<>();
//        int layer = 0;
//        for (Token token : tokens) {
//            switch (token.token) {
//                case ";" -> {
//                    lines.add(line);
//                    line = new ArrayList<>();
//                    for (int i = 0; i < layer; i++) line.add(new Token(levelSymbol, TokenType.Splitter, token.line));
//                }
//                case "{" -> {
//                    layer++;
//                    lines.add(line);
//                    line = new ArrayList<>();
//                    for (int i = 0; i < layer; i++) line.add(new Token(levelSymbol, TokenType.Splitter, token.line));
//                }
//                case "}" -> {
//                    layer--;
//                    if (line.size() != 0 && line.size() != layer + 1) lines.add(line);
//                    line = new ArrayList<>();
//                    for (int i = 0; i < layer; i++) line.add(new Token(levelSymbol, TokenType.Splitter, token.line));
//                }
//                default -> line.add(token);
//            }
//        }
//
//        List<Integer> shift = new ArrayList<>();
//        int s = 0;
//
//        int lvl;
//        int prevLvl = 0;
//        List<String> output = new ArrayList<>();
//        for (int i = 0; i < lines.size(); i++) {
//            lvl = 0;
//            line = lines.get(i);
//            while (line.get(lvl).token.equals(levelSymbol)) lvl++;
//            for (int j = lvl; j < prevLvl; j++) {
//                if (shift.size() > 0) s += shift.remove(shift.size() - 1);
//                output.add(output.size() - s, "end");
//            }
//            prevLvl = lvl;
//
//            for (int j = lvl; j < line.size(); j++) {
//                switch (line.get(j).token) {
//                    case "=" -> {
//                        String command;
//                        int oBrI = line.get(j - 1).token.indexOf("[");
//                        int cBrI = line.get(j - 1).token.indexOf("]");
//                        if (oBrI == -1 && cBrI == -1) {
//                            if (line.get(j + 1).type == TokenType.Constant) {
//                                output.add(output.size() + s,  "set " + line.get(j - 1).token + " " + line.get(j + 1).token);
//
//                            }
//                            else {
//
//                                SyntaxTree tree = new SyntaxTree();
//                                tree.buildTree(line.subList(j + 1, line.size()));
//                                tree.generateOpcode(output, s);
//
//                                String str = output.get(output.size() + s - 1);
//                                str = str.replace(tree.token.token, line.get(j - 1).token);
//                                output.set(output.size() + s - 1, str);
//                            }
//                        }
//                        else {
//                            if (oBrI != -1 && oBrI < cBrI) {
//                                command = "eset " + line.get(j - 1).token.substring(0, oBrI) + " " + line.get(j - 1).token.substring(oBrI + 1, cBrI) + " ";
//
//                                if (line.get(j + 1).type == TokenType.Constant) {
//                                    String var = "num" + Double.doubleToLongBits(Double.parseDouble(line.get(j + 1).token));
//                                    output.add(output.size() + s,  "set " + var + " " + line.get(j + 1).token);
//                                    output.add(output.size() + s, command + var);
//
//                                }
//                                else {
//
//                                    SyntaxTree tree = new SyntaxTree();
//                                    tree.buildTree(line.subList(j + 1, line.size()));
//                                    tree.generateOpcode(output, s);
//                                    output.add(output.size() + s, command + tree.token.token);
//                                }
//                            }
//                            else throw new CompilationException(line.get(j - 1).line, "incorrect variable definition");
//                        }
//                    }
//                    case "while" -> {
//                        SyntaxTree tree = new SyntaxTree();
//                        int start = output.size();
//                        tree.buildTree(line.subList(j + 2, line.size() - 1));
//                        tree.generateOpcode(output, s);
//                        int end = output.size();
//                        output.add(output.size() + s, "cyc " +  tree.token.token);
//                        output.addAll(output.subList(start, end));
//                        shift.add(end - start);
//                        s -= shift.get(shift.size() - 1);
//
//                    }
//                    case "if" -> {
//                        SyntaxTree tree = new SyntaxTree();
//                        tree.buildTree(line.subList(j + 2, line.size() - 1));
//                        tree.generateOpcode(output, s);
//                        output.add(output.size() + s, "if " +  tree.token.token);
//                    }
//                    case "kill" -> {
//                        String var = "num" + Double.doubleToLongBits(Double.parseDouble(line.get(j + 1).token));
//                        output.add(output.size() + s, "set " + var + " " + line.get(j + 1).token);
//                        output.add(output.size() + s, "kll " + var);
//                    }
//                }
//                if (line.get(j).type == TokenType.Variable) {
//
//                    if (j == line.size() - 1) {
//                        int oBrI = line.get(j).token.indexOf("[");
//                        int cBrI = line.get(j).token.indexOf("]");
//                        if (oBrI != -1) {
//                            if (oBrI < cBrI) {
//                                output.add(output.size() + s, "arr " + line.get(j).token.substring(0, oBrI) + " " + line.get(j).token.substring(oBrI + 1, cBrI));
//                            } else throw new CompilationException(line.get(j).line, "incorrect variable definition");
//                        }
//                    }
//                }
//
//            }
//        }
//
//        for (int j = 0; j < prevLvl; j++) {
//            if (shift.size() > 0) s += shift.remove(shift.size() - 1);
//            output.add(output.size() - s, "end");
//        }
//        return output;
//    }
//
//    private List<Variable> registrator(@NotNull List<String> opcode) {
//        List<Variable> variables = new ArrayList<>();
//
//        for (int i = 0; i < opcode.size(); i++) {
//            String[] s = opcode.get(i).split(" ");
//            if (s[0].equals("arr")) {
//                for (int j = 0; j < Double.parseDouble(s[2]); j++) {
//                    Variable variable = new Variable(s[1] + j);
//                    variable.creationCall = i;
//                    variables.add(variable);
//                }
//            }
//            else if (s[0].equals("eset")) {
//
//                List<Variable> arr = variables.stream().filter(variable -> variable.cipher.startsWith(s[1])).toList();
//                if (arr.size() == 0) throw new CompilationException(i, "array not defined");
//                else for (Variable var : arr) var.lastCall = Math.max(var.lastCall, i);
//
//                Variable var = variables.stream().filter(variable -> variable.cipher.equals(s[2])).findAny().orElse(null);
//                if (var == null) {
//                    Variable variable = new Variable(s[2]);
//                    variable.creationCall = i;
//                    variables.add(variable);
//                } else var.lastCall = Math.max(var.lastCall, i);
//
//            }
//            else {
//                int vEnd = s[0].equals("set") ? 1 : s.length - 1;
//
//                for (int j = 1; j <= vEnd; j++) {
//                    int finalJ = j;
//                    Variable var = variables.stream().filter(variable -> variable.cipher.equals(s[finalJ])).findAny().orElse(null);
//                    if (var == null) {
//                        Variable variable = new Variable(s[j]);
//                        variable.creationCall = i;
//                        variables.add(variable);
//                    } else var.lastCall = Math.max(var.lastCall, i);
//                }
//            }
//        }
//
//        return variables;
//    }
//
//    private void optimiser(@NotNull List<String> opcode, List<Variable> variables) {
//        int level = 0;
//        int deletedLines = 0;
//        for (int i = 0; i < opcode.size(); i++) {
//            String[] s = opcode.get(i).split(" ");
//
//            switch (s[0]) {
//                case "cyc" -> level++;
//                case "end" -> level = Math.max(0, level - 1);
//                case "eset" -> {
//                    boolean out = false;
//                    try {
//                        int v = Integer.parseInt(s[2]);
//                        Variable variable = variables.stream().filter(var -> var.cipher.equals(s[1] + v)).findAny().orElse(null);
//                        if (variable == null) out = true;
//                        else {
//                            opcode.remove(i);
//                            opcode.add(i, "set " + variable.cipher + " " + Double.longBitsToDouble(Long.parseLong(s[3].substring(3))));
//                        }
//
//                    } catch (Exception ignored) {
//                    }
//                    if (out) throw new CompilationException(i, "array out of bounds");
//                }
//                case "set" -> {
//                    if (s[1].startsWith("num")) {
//                        try {
//                            long v = Long.parseLong(s[1].substring(3));
//                            Variable variable = variables.stream().filter(var -> var.cipher.equals(s[1])).findAny().get();
//                            if (variable.creationCall != i + deletedLines) {
//                                opcode.remove(i);
//                                i--;
//                                deletedLines++;
//                            } else if (level > 0) {
//                                int j = 0;
//                                while (!opcode.get(i + j).split(" ")[0].equals("cyc")) j--;
//                                opcode.add(i + j - 1, opcode.remove(i));
//                                i--;
//                            }
//                        } catch (Exception ignored) {
//                        }
//                    }
//                }
//            }
//
//        }
//    }

    public static Token generateVariable(int line) {
        numerator = (numerator + 1) % Long.MAX_VALUE;
        return new Token("v" + numerator, TokenType.Variable, line);
    }
}
