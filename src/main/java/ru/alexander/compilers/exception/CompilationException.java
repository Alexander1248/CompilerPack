package ru.alexander.compilers.exception;

public class CompilationException extends RuntimeException {
    public CompilationException(String compiler, String message, int line) {
        super(compiler + ": Compilation error! Error in line " + line + " because " + message);
    }
}
