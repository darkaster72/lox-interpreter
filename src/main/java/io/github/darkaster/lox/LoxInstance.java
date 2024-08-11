package io.github.darkaster.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private final LoxClass clazz;
    private final Map<String, Object> members = new HashMap<>();

    public LoxInstance(LoxClass clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return "%s instance".formatted(clazz);
    }

    public Object get(Token name) {
        if (members.containsKey(name.lexeme)) {
            return members.get(name.lexeme);
        }

        LoxFunction method = findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    protected LoxFunction findMethod(String name) {
        return clazz.findMethod(name);
    }

    public void set(Token name, Object value) {
        members.put(name.lexeme, value);
    }
}
