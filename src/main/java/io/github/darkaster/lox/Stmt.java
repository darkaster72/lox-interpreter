package io.github.darkaster.lox;

import java.util.List;

abstract class Stmt {
    abstract <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {
        R visitBlockStmt(Block stmt);

        R visitExpressionStmt(Expression stmt);

        R visitFunctionStmt(Function stmt);

        R visitGetterStmt(Getter stmt);

        R visitClassStmt(Class stmt);

        R visitIfStmt(If stmt);

        R visitPrintStmt(Print stmt);

        R visitVarStmt(Var stmt);

        R visitWhileStmt(While stmt);

        R visitReturnStmt(Return stmt);
    }

    static class Block extends Stmt {
        final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    static class Expression extends Stmt {
        final Expr expression;

        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }
    }

    static class Function extends Stmt {
        final Token name;
        final List<Token> param;
        final List<Stmt> body;

        public Function(Token name, List<Token> param, List<Stmt> body) {
            this.name = name;
            this.param = param;
            this.body = body;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    static class Getter extends Function {
        public Getter(Token name, List<Stmt> body) {
            super(name, List.of(), body);
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetterStmt(this);
        }
    }

    static class Class extends Stmt {
        final Token name;
        final Expr.Variable superclass;
        final List<Stmt.Function> functions;

        public Class(Token name, Expr.Variable superclass, List<Stmt.Function> functions) {
            this.name = name;
            this.superclass = superclass;
            this.functions = functions;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitClassStmt(this);
        }
    }

    static class If extends Stmt {
        final Expr condition;
        final Stmt thenBranch;
        final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    static class Print extends Stmt {
        final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    static class Var extends Stmt {
        final Token name;
        final Expr initializer;

        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }
    }

    static class While extends Stmt {
        final Expr condition;
        final Stmt statement;

        public While(Expr condition, Stmt statement) {
            this.condition = condition;
            this.statement = statement;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    static class Return extends Stmt {
        final Token keyword;
        final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

}
