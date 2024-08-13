package io.github.darkaster.lox;

import java.util.List;
import java.util.Map;

public class LoxClass extends LoxInstance implements LoxCallable {
    private final Token name;
    private final Map<String, LoxFunction> methods;
    private final LoxClass superclass;

    public LoxClass(Token name, Map<String, LoxFunction> methods, LoxClass superclass) {
        super(null);
        this.name = name;
        this.methods = methods;
        this.superclass = superclass;
    }

    public String toString() {
        return name.lexeme;
    }

    @Override
    public int arity() {
        LoxFunction initializer = findMethod("init");
        if (initializer == null) return 0;
        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        LoxInstance instance = new LoxInstance(this);

        LoxFunction initializer = findMethod("init");
        if (initializer != null) {
            LoxFunction binded = initializer.bind(instance);
            binded.call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public LoxFunction findMethod(String name) {
        LoxFunction method = methods.get(name);
        if (method != null) return method;

        return superclass.findMethod(name);
    }

}
