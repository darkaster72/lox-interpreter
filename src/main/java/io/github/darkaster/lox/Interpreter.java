package io.github.darkaster.lox;

import java.util.List;
import java.util.Objects;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private Environment globals = new Environment();
    private Environment environment = globals;
    private boolean isReplMode = false;

    Interpreter() {
        globals.define("clock", new LoxCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis();
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        var value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        var left = evaluate(expr.left);
        var right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case EQUAL_EQUAL -> isEqual(left, right);
            case BANG_EQUAL -> !isEqual(left, right);
            case GREATER -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left > (double) right;
            }
            case GREATER_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left >= (double) right;
            }
            case LESS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left < (double) right;
            }
            case LESS_EQUAL -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left <= (double) right;
            }
            case MINUS -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left - (double) right;
            }
            case SLASH -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left / (double) right;
            }
            case STAR -> {
                checkNumberOperands(expr.operator, left, right);
                yield (double) left * (double) right;
            }
            case AND -> (boolean) left && (boolean) right;
            case OR -> (boolean) left || (boolean) right;
            case BANG -> !isTruthy(left);
            case PLUS -> {
                if (left instanceof String && right instanceof String) {
                    yield left + (String) right;
                }

                if (left instanceof String || right instanceof String) {
                    yield left.toString() + right.toString();
                }


                if (left instanceof Double && right instanceof Double) {
                    yield (Double) left + (Double) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be number or string");
            }
            default -> null;
        };
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        var right = evaluate(expr.right);

        return switch (expr.operator.type) {
            case MINUS -> -(double) right;
            case BANG -> !isTruthy(right);
            default -> null;
        };
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = expr.arguments.stream().map(this::evaluate).toList();

        if (callee instanceof LoxCallable function) {
            if (arguments.size() != function.arity()) {
                throw new RuntimeError(expr.paren, "Expected %d arguments but got %d.".formatted(function.arity(), arguments.size()));
            }
            return function.call(this, arguments);
        }

        throw new RuntimeError(expr.paren, "Can only call functions and classes.");
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return environment.get(expr.name);
    }

    private boolean isTruthy(Object value) {
        if (value == null) return false;
        if (value instanceof Boolean) return (boolean) value;
        return true;
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private boolean isEqual(Object a, Object b) {
        return Objects.equals(a, b);
    }

    private void checkNumberOperands(Token token, Object... operands) {
        for (var operand : operands) {
            if (!(operand instanceof Double)) {
                throw new RuntimeError(token, "Operands must be number");
            }
        }
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    private void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        // store the value
        if (isReplMode) {
            System.out.println(stringify(value));
        }
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        if (isTruthy(evaluate(stmt.condition)))
            execute(stmt.thenBranch);
        else if (stmt.elseBranch != null) execute(stmt.elseBranch);

        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);

        }
        environment.define(stmt.name, value);

        if (isReplMode) {
            System.out.println(stringify(value));
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.statement);
        }
        return null;
    }

    public void setReplMode(boolean isReplMode) {
        this.isReplMode = isReplMode;
    }
}
