package ru.alexander.compilers.tsl.data;

import org.jetbrains.annotations.NotNull;
import ru.alexander.compilers.tsl.data.tokens.Token;
import ru.alexander.compilers.tsl.TSLCompiler;
import ru.alexander.compilers.tsl.data.tokens.TokenType;

import java.util.List;

public class SyntaxTree {
    public Token token;

    public SyntaxTree left;
    public SyntaxTree right;

    public void buildTree(@NotNull List<Token> tokens) {
        if (tokens.size() > 0) {
            int iMin = 0;
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

            if (tokens.get(0).type == TokenType.MathFunction && minLayer != 0) {
                token = tokens.get(0);
                List<Token> vars = tokens.subList(2, tokens.size() - 1);
                int lvl = 0;
                int i = -1;
                for (int j = 0; j < vars.size(); j++) {
                    switch (vars.get(j).token) {
                        case "(" -> lvl++;
                        case ")" -> lvl--;
                        case "," -> {
                            if (lvl == 0) i = j;
                        }
                    }
                }

                left = new SyntaxTree();
                if (i == -1) left.buildTree(vars);
                else {
                    left.buildTree(vars.subList(0, i));

                    right = new SyntaxTree();
                    right.buildTree(vars.subList(i + 1, vars.size()));
                }
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
                                if (priority < 3) {
                                    priority = 3;
                                    iMin = i;
                                }
                            }
                            case "+", "-" -> {
                                if (priority < 2) {
                                    priority = 2;
                                    iMin = i;
                                }
                            }
                            case "*", "/", "%" -> {
                                if (priority < 1) {
                                    priority = 1;
                                    iMin = i;
                                }
                            }
                        }
                    }
                }

                left = new SyntaxTree();
                left.buildTree(tokens.subList(minLayer, Math.max(iMin, minLayer)));

                token = tokens.get(Math.max(iMin, minLayer));

                right = new SyntaxTree();
                right.buildTree(tokens.subList(Math.min(iMin + 1, tokens.size() - minLayer), tokens.size() - minLayer));
            }
        }
    }

    public void generateOpcode(List<String> lines, int s) {
        if (token != null) {
            if (left != null) left.generateOpcode(lines, s);
            if (right != null) right.generateOpcode(lines, s);

            if (token.type == TokenType.Constant) {
                String var = "num" + Double.doubleToLongBits(Double.parseDouble(token.token));
                lines.add(lines.size() + s, "set " + var + " " + token.token);
                token = new Token(var, TokenType.Variable, token.line);
            } else {
                Token var;
                if (token.type != TokenType.Variable)
                    var = TSLCompiler.generateVariable(token.line);
                else var = token;

                switch (token.token) {
                    case ">" -> lines.add(lines.size() + s, "mt " + var.token + " " + left.token.token + " " + right.token.token);
                    case ">=" -> lines.add(lines.size() + s, "met " + var.token + " " + left.token.token + " " + right.token.token);
                    case "<" -> lines.add(lines.size() + s, "lt " + var.token + " " + left.token.token + " " + right.token.token);
                    case "<=" -> lines.add(lines.size() + s, "let " + var.token + " " + left.token.token + " " + right.token.token);
                    case "==" -> lines.add(lines.size() + s, "eq " + var.token + " " + left.token.token + " " + right.token.token);

                    case "+" -> lines.add(lines.size() + s, "add " + var.token + " " + left.token.token + " " + right.token.token);
                    case "-" -> lines.add(lines.size() + s, "sub " + var.token + " " + left.token.token + " " + right.token.token);
                    case "*" -> lines.add(lines.size() + s, "mul " + var.token + " " + left.token.token + " " + right.token.token);
                    case "/" -> lines.add(lines.size() + s, "div " + var.token + " " + left.token.token + " " + right.token.token);
                    case "%" -> lines.add(lines.size() + s, "rem " + var.token + " " + left.token.token + " " + right.token.token);

                    case "pow" -> lines.add(lines.size() + s, "pow " + var.token + " " + left.token.token + " " + right.token.token);
                    case "log" -> lines.add(lines.size() + s, "log " + var.token + " " + left.token.token);
                    case "sin" -> lines.add(lines.size() + s, "sin " + var.token + " " + left.token.token);
                    case "cos" -> lines.add(lines.size() + s, "cos " + var.token + " " + left.token.token);
                    case "tan" -> lines.add(lines.size() + s, "tan " + var.token + " " + left.token.token);
                    case "abs" -> lines.add(lines.size() + s, "abs " + var.token + " " + left.token.token);
                }
                token = var;
            }
            left = null;
            right = null;
        }
    }

    @Override
    public String toString() {
        return drawTree(0);
    }

    private String drawTree(int level) {
        String str = "";
        if (left != null) str += left.drawTree(level + 1);
        if (token != null) str += " ".repeat(level) + token.token + "\n";
        if (right != null) str += right.drawTree(level + 1);
        return str;
    }
}
