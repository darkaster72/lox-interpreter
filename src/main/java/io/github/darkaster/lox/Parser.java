package io.github.darkaster.lox;

import java.util.ArrayList;
import java.util.List;

import static io.github.darkaster.lox.TokenType.*;

/*
 * Parser handles the syntax grammar
 * this is a “recursive descent parser”  because it walks down the grammar.
 * In a top-down parser, you reach the lowest-precedence expressions first because
 * they may in turn contain subexpressions of higher precedence.
 *
 * program        → statement* EOF ;
 * statement      → exprStmt | ifStmt | forStmt | printStmt | block | whileStmt | returnStmt;
 * forStmt        → for "(" (varStmt | expression)? ";" expression? ";" expression? ")" statement;
 * whileStmt      → while "(" expression ")" statement;
 * ifStmt         → "if" "(" expression ")" statement ( "else" statement )? ;
 * exprStmt       → expression ";" ;
 * printStmt      → "print" expression ";" ;
 * returnStmt     → "return" expression? ";" ;
 * block          → "{" declaration* "}" ;
 * declaration    → classDecl | funDecl | varDecl | statement ;
 * classDecl      → "class" IDENTIFIER ("<" IDENTIFIER )? "{" function* "}"
 * funDecl        → "fun" function;
 * function       → IDENTIFIER ("(" parameters? ")")? block ;
 * parameters     → IDENTIFIER ("," IDENTIFIER)* ;
 * expression     → assignment ;
 * assignment     → (call ".")? IDENTIFIER "=" assignment | logical_or ;
 * logical_or     → logical_and ("or" logical_and)*;
 * logical_and    → equality ("and" equality)*;
 * equality       → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term           → factor ( ( "-" | "+" ) factor )* ;
 * factor         → unary ( ( "/" | "*" ) unary )* ;
 * unary          → ( "!" | "-" ) unary | call ;
 * call           → primary ( "(" arguments? ")" | "." IDENTIFIER)*;
 * arguments      → expression, ("," expression)*
 * primary        → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER | "super" "." IDENTIFIER;
 * */
class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    //    Given a valid sequence of tokens, produce a corresponding syntax tree.
    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return varDeclaration();
            return statement();
        } catch (ParseError e) {
            synchronize();
            return null;
        }

    }

    private Stmt classDeclaration() {
        List<Stmt.Function> methods = new ArrayList<>();
        Expr.Variable superclass = null;

        Token name = consume(IDENTIFIER, "Identifier expected for class name.");

        if (match(LESS)) {
            consume(IDENTIFIER, "Identifier  expected after '<'.");
            superclass = new Expr.Variable(previous());
        }

        consume(LEFT_BRACE, "Expected '{' for class block.");

        while (!(check(RIGHT_BRACE) || isAtEnd())) {
            if (match(STATIC)) methods.add(function("static"));
            else methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expected '}' after class block");

        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt.Function function(String kind) {
        Token name = consume(IDENTIFIER, "Expect %s name.".formatted(kind));
        List<Token> params = new ArrayList<>();

        if (match(LEFT_BRACE)) {
            List<Stmt> body = block();
            return new Stmt.Getter(name, body);
        }

        consume(LEFT_PAREN, "Expected '(' after '%s' declaration".formatted(kind));

        if (!check(RIGHT_PAREN)) {
            do {
                if (params.size() >= 255) {
                    error(peek(), "Can't have more than 255 parameters.");
                }
                params.add(consume(IDENTIFIER, "Expect parameter name"));
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expected ')' after parameters");

        consume(LEFT_BRACE, "Expect '{' before " + kind + " body.");
        List<Stmt> body = block();

        return new Stmt.Function(name, params, body);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        Expr initializer = null;

        if (match(EQUAL)) {
            initializer = expression();
        }
        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    // syntax grammar
    // expression → equality ;
    private Expr expression() {
        return assignment();
    }

    //  assignment → (call ".")? IDENTIFIER "=" assignment | logical_or ;
    private Expr assignment() {
        Expr expr = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable variable) {
                return new Expr.Assign(variable.name, value);
            } else if (expr instanceof Expr.Get getter) {
                return new Expr.Set(getter.object, getter.name, value);
            }

            throw error(equals, "Invalid assignment target");
        }

        return expr;
    }

    // logical_or → logical_and ("or" logical_and)*;
    private Expr or() {
        Expr expr = and();

        if (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    // logical_and → equality ("and" equality)*;
    private Expr and() {
        Expr expr = equality();

        if (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }
        return expr;
    }

    // syntax grammar
    // equality  → comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            var operator = previous();
            var right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    // syntax grammar
    //    comparison     → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // syntax grammar
    // term → factor ( ( "-" | "+" ) factor )* ;
    private Expr term() {
        Expr expr = factor();

        while (match(PLUS, MINUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    // syntax grammar
    // factor → unary ( ( "/" | "*" ) unary )* ;
    private Expr factor() {
        Expr expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }


    // syntax grammar
    // unary → ( "!" | "-" ) unary | call ;
    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();

            return new Expr.Unary(operator, right);
        }

        return call();
    }

    //  call → primary ( "(" arguments? ")" | "." IDENTIFIER)*;
    private Expr call() {
        Expr expr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expr = finishCall(expr);
            } else if (match(DOT)) {
                var name = consume(IDENTIFIER, "Expected 'identifier' after '.'");
                expr = new Expr.Get(expr, name);
            } else break;
        }

        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> arguments = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 255) {
                    error(peek(), "Can't have more than 255 arguments.");
                }
                arguments.add(expression());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "Expected ')' after arguments");

        return new Expr.Call(callee, paren, arguments);
    }

    // syntax grammar
    // primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" | IDENTIFIER | "super" "." IDENTIFIER;
    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(STRING, NUMBER)) return new Expr.Literal(previous().literal);

        if (match(THIS)) return new Expr.This(previous());

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "Expect '.' after super.");
            Token method = consume(IDENTIFIER, "Expect identifier after 'super.'");
            return new Expr.Super(keyword, method);
        }

        if (match(IDENTIFIER)) return new Expr.Variable(previous());

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private boolean match(TokenType... tokenTypes) {
        for (var tokenType : tokenTypes) {
            if (check(tokenType)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token consume(TokenType tokenType, String message) {
        if (check(tokenType)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Lox.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private Stmt statement() {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return thenBlock();
        if (match(FOR)) return forStatement();
        if (match(WHILE)) return whileStatement();
        if (match(IF)) return ifStatement();
        if (match(RETURN)) return returnStatement();

        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token token = previous();
        Expr expr = null;
        if (!check(SEMICOLON)) {
            expr = expression();
        }
        consume(SEMICOLON, "';' expected after return value");
        return new Stmt.Return(token, expr);
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'for'");
        Stmt initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expr condition;
        if (match(SEMICOLON)) {
            condition = null;
        } else {
            condition = expression();
            consume(SEMICOLON, "Expected ';' after 'for' condition");
        }

        Expr increment;
        if (match(RIGHT_PAREN)) {
            increment = null;
        } else {
            increment = expression();
            consume(RIGHT_PAREN, "Expected ')' after 'for' condition");
        }

        Stmt body = statement();

        if (increment != null) {
            body = new Stmt.Block(List.of(body, new Stmt.Expression(increment)));
        }
        if (condition == null) condition = new Expr.Literal(true);
        body = new Stmt.While(condition, body);

        if (initializer != null) {
            body = new Stmt.Block(List.of(initializer, body));
        }

        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'while'");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after 'while' condition");
        Stmt stmt = statement();

        return new Stmt.While(condition, stmt);
    }

    // ifStmt → "if" "(" expression ")" statement ( "else" statement )? ;
    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after 'if'");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after 'if' condition");

        Stmt thenBranch = statement();
        Stmt elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt thenBlock() {
        return new Stmt.Block(block());
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }


    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expr);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private static class ParseError extends RuntimeException {
    }
}