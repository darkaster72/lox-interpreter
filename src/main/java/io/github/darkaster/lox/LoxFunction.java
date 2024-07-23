package io.github.darkaster.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;

    public LoxFunction(Stmt.Function declaration) {
        this.declaration = declaration;
    }

    @Override
    public int arity() {
        return declaration.param.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment env = new Environment(interpreter.globals);

        for (int i = 0; i < arity(); i++) {
            env.define(declaration.param.get(i), arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, env);
        } catch (Return returnValue) {
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
