package ru.alexander.compilers;

import java.util.List;

public interface Compiler<T> {
    T compile(String code);
}
