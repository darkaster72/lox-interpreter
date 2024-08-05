package io.github.darkaster.lox;

public class LoxClass {
    private final Token name;

    public LoxClass(Token name) {
        this.name = name;
    }

    public String toString() {
        return name.lexeme;
    }
}
