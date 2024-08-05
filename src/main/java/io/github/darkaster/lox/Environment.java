package io.github.darkaster.lox;

import java.util.HashMap;
import java.util.Map;

public class Environment {
    private final Environment enclosing;
    private final Map<String, Object> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(Token name, Object value) {
        define(name.lexeme, value);
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) return values.get(name.lexeme);

        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    public Object getAt(Integer distance, Token name) {
        return ancestor(distance).get(name);
    }

    private Environment ancestor(Integer distance) {
        Environment env = this;
        for (int i = 0; i < distance; i++) {
            env = env.enclosing;
        }
        return env;
    }

    public void assignAt(Integer distance, Token name, Object value) {
        ancestor(distance).assign(name, value);
    }
}
