package io.github.darkaster.lox;

import java.util.List;
import java.util.Map;

public class LoxClass implements LoxCallable {
    private final Token name;
    private final Map<String, LoxFunction> methods;

    public LoxClass(Token name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
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

    public LoxFunction findMethod(String name) {
        return methods.get(name);
    }
}
