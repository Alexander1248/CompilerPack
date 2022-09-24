package ru.alexander.compilers.exception;

public class VMException extends RuntimeException {
    public VMException(String vm, String message) {
        super("Virtual machine error! " + vm + " stopped because " + message);
    }
    public VMException(String vm, int code) {
        super("Virtual machine stopped! " + vm + " stopped with code " + code);
    }
}
