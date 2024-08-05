package io.github.darkaster.lox;

import java.util.List;

public class LoxClass implements LoxCallable {
    private final Token name;

    public LoxClass(Token name) {
        this.name = name;
    }

    public String toString() {
        return name.lexeme;
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        return new LoxInstance(this);
    }
}
