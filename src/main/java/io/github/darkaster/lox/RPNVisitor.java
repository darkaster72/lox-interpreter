package io.github.darkaster.lox;

/*
 * A reverse polish notation vistor
 * // expr: (1 + 2) * (4 - 3)
 * // result: 1 2 + 4 3 - *
 * */
public class RPNVisitor implements Expr.Visitor<String> {
    public static void main(String[] args) {
//        (1 + 2) * (4 - 3)
        Expr expression = new Expr.Binary(
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(1),
                                new Token(TokenType.PLUS, "+", null, 1),
                                new Expr.Literal(2)
                        )
                ),
                new Token(TokenType.STAR, "*", null, 1),
                new Expr.Grouping(
                        new Expr.Binary(
                                new Expr.Literal(4),
                                new Token(TokenType.MINUS, "-", null, 1),
                                new Expr.Literal(5)
                        )));

        System.out.println(new RPNVisitor().print(expression));
    }

    private String print(Expr expression) {
        return expression.accept(this);
    }

    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return "";
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        var sb = new StringBuilder();
        sb.append(expr.left.accept(this));
        sb.append(" ");
        sb.append(expr.right.accept(this));
        sb.append(" ").append(expr.operator.lexeme);

        return sb.toString();
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return expr.expression.accept(this);
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) return "nil";
        return expr.value.toString();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return expr.right.accept(this) + " " + expr.operator.lexeme;
    }

    @Override
    public String visitVariableExpr(Expr.Variable expr) {
        return "";
    }
}
