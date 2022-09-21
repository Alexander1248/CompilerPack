package ru.alexander.compilers.tsl.data.tokens;

public enum TokenType {
    ResWord,    //ключевое (зарезервированное) слово
    Operator,   //оператор
    Constant,   //числовая константа
    Variable,   //переменная
    Function,   //функция
    MathFunction,   //функция
    Bracket,    //скобки
    Splitter    //разделитель
}
