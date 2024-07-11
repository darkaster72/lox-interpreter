package io.github.darkaster.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.github.darkaster.lox.TokenType.*;

public class Scanner {
    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();

        switch (c) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(matchNext('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(matchNext('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(matchNext('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(matchNext('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (matchNext('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else if (matchNext('*')) {
                    while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
                        if (peek() == '\n') line++;
                        advance();
                    }
                    if (isAtEnd()) Lox.error(line, "Unclosed comment");
                    else {
                        advance(); // consume *
                        advance(); // consume /
                    }
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '"':
                string();
                break;
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Lox.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private void identifier() {
        while (isAlphanumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType tokenType = keywords.getOrDefault(text, IDENTIFIER);

        addToken(tokenType);
    }

    private boolean isAlphanumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return Character.isAlphabetic(c) || c == '_';
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && isDigit(peekNext())) {

            do advance();
            while (isDigit(peek()));
        }

        Double value = Double.parseDouble(source.substring(start, current));
        addToken(NUMBER, value);
    }

    private char peekNext() {
        if (isAtEndNext()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean isAtEndNext() {
        return current + 1 >= source.length();
    }

    private boolean isDigit(char c) {
        return Character.isDigit(c);
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Lox.error(line, "Unterminated String");
        }

        // The closing ".
        advance();

        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private boolean matchNext(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char advance() {
        return source.charAt(current++);
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}
