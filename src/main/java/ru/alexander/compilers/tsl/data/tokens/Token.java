package ru.alexander.compilers.tsl.data.tokens;

public class Token {
    public TokenType type;
    public String token;
    public int line;

    public Token(String token, TokenType type, int line) {
        this.type = type;
        this.token = token;
        this.line = line;
    }

    @Override
    public String toString() {
        return token + " ".repeat(Math.max(0, 30 - token.length()))+ "[" + type + "]";
    }
}
