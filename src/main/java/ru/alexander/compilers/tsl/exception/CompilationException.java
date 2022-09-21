package ru.alexander.compilers.tsl.exception;

public class CompilationException extends RuntimeException {
    public CompilationException(int line, String message) {
        super("Compilation error! Error in line " + line + " because " + message);
    }
}
