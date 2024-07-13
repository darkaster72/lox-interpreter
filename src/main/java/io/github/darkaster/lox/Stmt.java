package io.github.darkaster.lox;

import java.util.List;

abstract class Stmt {
		interface Visitor<R> {
			R visitExpressionStmt(Expression stmt);
			R visitPrintStmt(Print stmt);
		}
		abstract <R> R accept(Visitor<R> visitor);
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

}
