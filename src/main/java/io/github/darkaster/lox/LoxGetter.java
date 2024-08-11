package io.github.darkaster.lox;

import java.util.List;

public class LoxGetter extends LoxFunction {

    public LoxGetter(Stmt.Getter stmt, Environment environment) {
        super(stmt, environment, false);
    }

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public LoxFunction bind(LoxInstance loxInstance) {
        Environment env = new Environment(closure);
        env.define("this", loxInstance);
        return new LoxGetter((Stmt.Getter) declaration, env);
    }

    public Object call(Interpreter interpreter) {
        return call(interpreter, List.of());
    }
}
