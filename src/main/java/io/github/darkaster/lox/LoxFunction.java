package io.github.darkaster.lox;

import java.util.List;

public class LoxFunction implements LoxCallable {
    protected final Stmt.Function declaration;
    protected final Environment closure;
    protected final boolean isInitializer;

    public LoxFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
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
            if (isInitializer) return closure.getAt(0, "this");
            return returnValue.value;
        }
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    public LoxFunction bind(LoxInstance loxInstance) {
        Environment env = new Environment(closure);
        env.define("this", loxInstance);
        return new LoxFunction(declaration, env, isInitializer);
    }

}
