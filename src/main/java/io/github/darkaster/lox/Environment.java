package io.github.darkaster.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();

    public void define(Token name, Object value) {
        values.put(name.lexeme, value);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public Object get(Token name) {
        if (!values.containsKey(name.lexeme)) {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
        return values.get(name.lexeme);
    }
}
