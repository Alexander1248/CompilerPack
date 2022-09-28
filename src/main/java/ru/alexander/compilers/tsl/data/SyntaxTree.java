package ru.alexander.compilers.tsl.data;

import org.jetbrains.annotations.NotNull;
import ru.alexander.compilers.exception.CompilationException;
import ru.alexander.compilers.tsl.data.tokens.Token;
import ru.alexander.compilers.tsl.compilers.AetherTSL;
import ru.alexander.compilers.tsl.data.tokens.TokenType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SyntaxTree {
    public Token token;

    public SyntaxTree[] branches;

    public boolean buildTree(@NotNull List<Token> tokens) {
        if (tokens.size() > 0) {
            int layer = 0;
            int minLayer;
            if (tokens.size() > 1) {
                minLayer = Integer.MAX_VALUE;
                for (int i = 0; i < tokens.size(); i++) {
                    Token value = tokens.get(i);
                    if (value.type != TokenType.MathFunction && i < tokens.size() - 1) {
                        switch (value.token) {
                            case "(" -> layer++;
                            case ")" -> layer--;
                        }
                        if (layer < minLayer) minLayer = layer;
                    }
                }
            } else minLayer = 0;
            layer = 0;

            int iMin = minLayer;
            if (tokens.get(0).type == TokenType.MathFunction && minLayer != 0) {
                token = tokens.get(0);
                List<Token> vars = tokens.subList(1, tokens.size());
                int lvl = 0;
                int i = 0;
                List<SyntaxTree> trees = new ArrayList<>();
                for (int j = 0; j < vars.size(); j++) {
                    switch (vars.get(j).token) {
                        case "(" -> lvl++;
                        case ")" -> {
                            if (lvl == 1) {
                                SyntaxTree tree = new SyntaxTree();
                                tree.buildTree(vars.subList(i + 1, j));
                                trees.add(tree);
                            }
                            lvl--;
                        }
                        case "," -> {
                            if (lvl == 1) {
                                SyntaxTree tree = new SyntaxTree();
                                tree.buildTree(vars.subList(i + 1, j));
                                trees.add(tree);
                                i = j;
                            }
                        }
                    }
                }

                branches = trees.toArray(new SyntaxTree[0]);
            } else {
                int priority = 0;
                for (int i = tokens.size() - 1; i >= 0; i--) {
                    switch (tokens.get(i).token) {
                        case "(" -> layer--;
                        case ")" -> layer++;
                    }
                    if (layer == minLayer) {
                        switch (tokens.get(i).token) {
                            case ">", ">=", "<", "<=" -> {
                                if (priority < 7) {
                                    priority = 7;
                                    iMin = i;
                                }
                            }
                            case "+", "-", "||", "&&" -> {
                                if (priority < 6) {
                                    priority = 6;
                                    iMin = i;
                                }
                            }
                            case "*", "/", "%" -> {
                                if (priority < 5) {
                                    priority = 5;
                                    iMin = i;
                                }
                            }
                            case "."-> {
                                if (priority < 1) {
                                    priority = 1;
                                    iMin = i;
                                }
                            }
                        }
                    }
                }
                boolean sqBr = false;
                if (iMin == 0) {
                    for (int i = tokens.size() - 1; i >= 0; i--) {
                        if ("[".equals(tokens.get(i).token)) {
                            sqBr = true;
                            iMin = i + 1;
                            break;
                        }
                    }
                }

                branches = new SyntaxTree[2];
                if (sqBr) {
                    if (tokens.get(iMin + 3).token.equals("]")) {
                        token = tokens.get(iMin);

                        branches[0] = new SyntaxTree();
                        branches[0].token = tokens.get(iMin + 2);

                        branches[1] = new SyntaxTree();
                    }
                    else if (tokens.get(iMin + 5).token.equals("]")) {
                        token = tokens.get(iMin);

                        branches[0] = new SyntaxTree();
                        branches[0].token = tokens.get(iMin + 2);

                        branches[1] = new SyntaxTree();
                        branches[1].token = tokens.get(iMin + 4);
                    }
                }
                else {
                    branches[0] = new SyntaxTree();
                    branches[0].buildTree(tokens.subList(minLayer, iMin));

                    token = tokens.get(iMin);

                    branches[1] = new SyntaxTree();
                    branches[1].buildTree(tokens.subList(iMin + 1, tokens.size() - minLayer));
                }
            }
            return true;
        }
        else return false;
    }

    public void generateOpcode(List<String> varName,List<String> varType,List<String> lines, int s) {
        if (token != null && token.type != TokenType.ResWord) {

            if (token.type == TokenType.Constant) {
                if (branches[0].token == null) {
                    String var = "num_" + Double.doubleToLongBits(Double.parseDouble(token.token));
                    lines.add(lines.size() + s, "var " + var);
                    lines.add(lines.size() + s, "set " + var + " " + token.token);
                    token = new Token(var, TokenType.Variable, token.line);
                }
                else {
                    if (branches[1].token == null) {
                        String var = "vec2_"
                                + Double.doubleToLongBits(Double.parseDouble(token.token))
                                + Double.doubleToLongBits(Double.parseDouble(branches[0].token.token));
                        lines.add(lines.size() + s, "vec2 " + var);
                        lines.add(lines.size() + s, "set " + var + " " + token.token+ " " + branches[0].token.token);
                        token = new Token(var, TokenType.Variable, token.line);
                    }
                    else {
                        String var = "vec3_"
                                + Double.doubleToLongBits(Double.parseDouble(token.token))
                                + Double.doubleToLongBits(Double.parseDouble(branches[0].token.token))
                                + Double.doubleToLongBits(Double.parseDouble(branches[1].token.token));
                        lines.add(lines.size() + s, "vec3 " + var);
                        lines.add(lines.size() + s, "set " + var + " " + token.token + " " + branches[0].token.token + " " + branches[1].token.token);
                        token = new Token(var, TokenType.Variable, token.line);
                    }
                }
            } else {
                for (int i = 0; i < branches.length; i++)
                    if (branches[i] != null)
                        branches[i].generateOpcode(varName, varType, lines, s);

                Token var;
                if (token.type != TokenType.Variable) {
                    var = AetherTSL.generateVariable(token.line);
                    if (branches[0] != null && branches[0].token.token.startsWith("vec2_")) {
                        var.token = "vec2_" + var.token;
                        lines.add(lines.size() + s, "vec2 " + var.token);

                    }
                    else if (branches[0] != null &&  branches[0].token.token.startsWith("vec3_")) {
                        var.token = "vec3_" + var.token;
                        lines.add(lines.size() + s, "vec3 " + var.token);
                    }
                    else {
                        var.token = "var_" + var.token;
                        lines.add(lines.size() + s, "var " + var.token);
                    }
                    if (token.token.equals("."))
                        lines.add(lines.size() + s, "gv" + branches[1].token.token + " " + var.token + " " + branches[0].token.token);
                }
                else {
                    if (token.token.contains("[")) {
                        int oBrI = token.token.indexOf("[");
                        int cBrI = token.token.indexOf("]");
                        if (oBrI < cBrI) {
                            String name = token.token.substring(0, oBrI);
                            String index = token.token.substring(oBrI + 1, cBrI);
                            List<Token> indexTokens = AetherTSL.lexicalAnalyser(index);
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
                            tree.generateOpcode(varName, varType, lines, s);

                            var = AetherTSL.generateVariable(token.line);
                            if (token.token.startsWith("vec2_")) {
                                var.token = "vec2_" + var.token;
                                lines.add(lines.size() + s, "vec2 " + var.token);
                            }
                            else if (token.token.startsWith("vec3_")) {
                                var.token = "vec3_" + var.token;
                                lines.add(lines.size() + s, "vec3 " + var.token);
                            }
                            else {
                                var.token = "var_" + var.token;
                                lines.add(lines.size() + s, "var " + var.token);
                            }
                            lines.add(lines.size() + s, "geta " + var.token + " " + name + " " + tree.token.token);
                        }
                        else throw new CompilationException("AetherTSL", "Incorrect array initialization", token.line);
                    }
                    else var = token;
                }

                switch (token.token) {
                    case "intersect" -> lines.add(lines.size() + s, "itct " + var.token + " " + branches[0].token.token + " " + branches[1].token.token + " " + branches[2].token.token + " " + branches[3].token.token);
                    case "pointInCut" -> lines.add(lines.size() + s, "pinc " + var.token + " " + branches[0].token.token + " " + branches[1].token.token + " " + branches[2].token.token);
                    case "lerp" -> lines.add(lines.size() + s, "lerp " + var.token + " " + branches[0].token.token + " " + branches[1].token.token + " " + branches[2].token.token);

                    case ">" -> lines.add(lines.size() + s, "mt " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case ">=" -> lines.add(lines.size() + s, "met " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "<" -> lines.add(lines.size() + s, "lt " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "<=" -> lines.add(lines.size() + s, "let " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "==" -> lines.add(lines.size() + s, "eq " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);

                    case "+" -> lines.add(lines.size() + s, "add " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "-" -> lines.add(lines.size() + s, "sub " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "*" -> lines.add(lines.size() + s, "mul " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "/" -> lines.add(lines.size() + s, "div " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "%" -> lines.add(lines.size() + s, "rem " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);

                    case "&&" -> lines.add(lines.size() + s, "and " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "||" -> lines.add(lines.size() + s, "or " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);

                    case "reflect" -> {
                        if (var.token.startsWith("vec3_") || var.token.startsWith("vec2_"))
                            lines.add(lines.size() + s, "vrfl " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                        else
                            lines.add(lines.size() + s, "arfl " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    }

                    case "dot" -> lines.add(lines.size() + s, "dot " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "cross" -> lines.add(lines.size() + s, "crs " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "ray" -> lines.add(lines.size() + s, "cray " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "normalOf" -> lines.add(lines.size() + s, "getn " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);

                    case "pow" -> lines.add(lines.size() + s, "pow " + var.token + " " + branches[0].token.token + " " + branches[1].token.token);
                    case "log" -> lines.add(lines.size() + s, "log " + var.token + " " + branches[0].token.token);
                    case "sqrt" -> lines.add(lines.size() + s, "sqrt " + var.token + " " + branches[0].token.token);
                    case "sin" -> lines.add(lines.size() + s, "sin " + var.token + " " + branches[0].token.token);
                    case "cos" -> lines.add(lines.size() + s, "cos " + var.token + " " + branches[0].token.token);
                    case "tan" -> lines.add(lines.size() + s, "tan " + var.token + " " + branches[0].token.token);
                    case "abs" -> lines.add(lines.size() + s, "abs " + var.token + " " + branches[0].token.token);
                    case "length" -> lines.add(lines.size() + s, "vlen " + var.token + " " + branches[0].token.token);
                }
                token = var;
            }
            Arrays.fill(branches, null);
        }
    }
}
