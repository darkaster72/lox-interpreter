package io.github.darkaster.lox;

import java.util.HashMap;
import java.util.Map;

public class LoxInstance {
    private final LoxClass clazz;
    private final Map<String, Object> map = new HashMap<>();

    public LoxInstance(LoxClass clazz) {
        this.clazz = clazz;
    }

    @Override
    public String toString() {
        return "%s instance".formatted(clazz);
    }

    public Object get(Token name) {
        if (map.containsKey(name.lexeme)) {
            return map.get(name.lexeme);
        }

        LoxFunction method = clazz.findMethod(name.lexeme);
        if (method != null) return method.bind(this);

        throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'.");
    }

    public void set(Token name, Object value) {
        map.put(name.lexeme, value);
    }
}
