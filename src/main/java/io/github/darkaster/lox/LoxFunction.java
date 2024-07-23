package io.github.darkaster.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    private final Stmt.Function declaration;
    private final Environment closure;

    public LoxFunction(Stmt.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.param.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment env = new Environment(closure);

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
