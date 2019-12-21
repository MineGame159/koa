package minegame159.koa.ast;

import minegame159.koa.Error;
import minegame159.koa.Lexer;
import minegame159.koa.Token;

import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    public static class Result {
        public ArrayList<Stmt> stmts = new ArrayList<>();
        public ArrayList<Error> errors = new ArrayList<>(0);

        public boolean hadError() {
            return errors.size() > 0;
        }
        public void printErrors() {
            for (int i = 0; i < errors.size(); i++) System.out.println(errors.get(i));
        }
    }

    private static class ParseError extends RuntimeException {}

    private Lexer lexer;
    private Token previous;
    private Token current;
    private Token next;
    private Result result = new Result();

    private Parser(String source) {
        lexer = new Lexer(source);

        next = lexer.scanToken();

        current = next;
        next = lexer.scanToken();

        parse();
    }

    public static Result parse(String source) {
        return new Parser(source).result;
    }

    private void parse() {
        try {
            if (current.type == Token.Type.Error) error(current, current.lexeme);
            if (next.type == Token.Type.Error) error(next, next.lexeme);

            while (!isAtEnd()) {
                    Stmt stmt = declaration();
                    if (stmt instanceof Stmt.Var && ((Stmt.Var) stmt).initializer instanceof Expr.Function) result.stmts.add(0, stmt);
                    else result.stmts.add(stmt);
            }
        } catch (ParseError e) {
            synchronize();
        }
    }

    // Statements

    private Stmt declaration() {
        if (current.type == Token.Type.Var) {
            advance();
            return variableDeclaration();
        } else return statement();
    }

    private Stmt variableDeclaration() {
        int line = current.line;
        Token name = consume(Token.Type.Identifier, "Expected variable name.");

        Expr initializer = null;
        if (match(Token.Type.Equal)) initializer = expression();

        return new Stmt.Var(line, name, initializer);
    }

    private Stmt statement() {
        switch (current.type) {
            case If:        advance(); return ifStatement();
            case While:     advance(); return whileStatement();
            case For:       advance(); return forStatement();
            case Break:     advance(); return breakStatement();
            case Continue:  advance(); return continueStatement();
            case Return:    advance(); return returnStatement();
            default:        return expressionStatement();
        }
    }

    private Stmt blockStatement() {
        int line = current.line;
        ArrayList<Stmt> stmts = new ArrayList<>();

        while (!check(Token.Type.RightBrace) && !isAtEnd()) stmts.add(declaration());

        consume(Token.Type.RightBrace, "Expected '}' after block.");
        stmts.trimToSize();
        return new Stmt.Block(line, stmts);
    }

    private Stmt ifStatement() {
        int line = current.line;
        Expr condition = expression();
        Stmt thenBranch = blockOrStatement();
        Stmt elseBranch = null;
        if (match(Token.Type.Else)) elseBranch = blockOrStatement();
        return new Stmt.If(line, condition, thenBranch, elseBranch);
    }

    private Stmt whileStatement() {
        int line = current.line;
        Expr condition = expression();
        Stmt body = blockOrStatement();
        return new Stmt.While(line, condition, body);
    }

    private Stmt forStatement() {
        int line = current.line;
        consume(Token.Type.LeftParen, "Expected '(' after 'for'.");
        Stmt initializer = null;
        switch (current.type) {
            case Semicolon: advance(); initializer = null; break;
            case Var:       advance(); initializer = variableDeclaration(); break;
            default:        expressionStatement(); break;
        }
        Expr condition = null;
        if (!check(Token.Type.Semicolon)) condition = expression();
        consume(Token.Type.Semicolon, "Expected ';' after condition");
        Expr increment = null;
        if (!check(Token.Type.RightParen)) increment = expression();
        consume(Token.Type.RightParen, "Expected ')' after for clauses.");
        Stmt body = blockOrStatement();
        return new Stmt.For(line, initializer, condition, increment, body);
    }

    private Stmt breakStatement() {
        int line = current.line;
        return new Stmt.Break(line);
    }

    private Stmt continueStatement() {
        int line = current.line;
        return new Stmt.Continue(line);
    }

    private Stmt expressionStatement() {
        int line = current.line;
        Expr expr = expression();
        return new Stmt.Expression(line, expr);
    }

    private Stmt returnStatement() {
        int line = current.line;
        Expr value = expression();
        return new Stmt.Return(line, value);
    }

    // Expressions

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        int line = current.line;
        Expr expr = or();

        while (match(Token.Type.Equal, Token.Type.PlusEqual, Token.Type.MinusEqual, Token.Type.StarEqual, Token.Type.SlashEqual, Token.Type.PercentageEqual, Token.Type.PlusPlus, Token.Type.MinusMinus)) {
            Token operator = previous;
            Expr value = null;
            if (previous.type != Token.Type.PlusPlus && previous.type != Token.Type.MinusMinus) value = assignment();

            if (expr instanceof Expr.Variable) return new Expr.Assign(line, ((Expr.Variable) expr).name, operator, value);
            else if (expr instanceof Expr.Get) return new Expr.Set(line, ((Expr.Get) expr).object, ((Expr.Get) expr).name, operator, value);

            error(operator, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        int line = current.line;
        Expr expr = and();

        while (match(Token.Type.Or)) {
            Token operator = previous;
            Expr right = and();
            expr = new Expr.Logical(line, expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        int line = current.line;
        Expr expr = equality();

        while (match(Token.Type.And)) {
            Token operator = previous;
            Expr right = equality();
            expr = new Expr.Logical(line, expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        int line = current.line;
        Expr expr = comparison();

        while (match(Token.Type.EqualEqual, Token.Type.BangEqual)) {
            Token operator = previous;
            Expr right = comparison();
            expr = new Expr.Binary(line, expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        int line = current.line;
        Expr expr = addition();

        while (match(Token.Type.Greater, Token.Type.GreaterEqual, Token.Type.Less, Token.Type.LessEqual)) {
            Token operator = previous;
            Expr right = addition();
            expr = new Expr.Binary(line, expr, operator, right);
        }

        return expr;
    }

    private Expr addition() {
        int line = current.line;
        Expr expr = multiplication();

        while (match(Token.Type.Minus, Token.Type.Plus)) {
            Token operator = previous;
            Expr right = multiplication();
            expr = new Expr.Binary(line, expr, operator, right);
        }

        return expr;
    }

    private Expr multiplication() {
        int line = current.line;
        Expr expr = unary();

        while (match(Token.Type.Star, Token.Type.Slash, Token.Type.Percentage)) {
            Token operator = previous;
            Expr right = unary();
            expr = new Expr.Binary(line, expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        int line = current.line;

        if (match(Token.Type.Bang, Token.Type.Minus)) {
            Token operator = previous;
            Expr right = unary();
            return new Expr.Unary(line, operator, right);
        }

        return call();
    }

    private Expr call() {
        int line = current.line;
        Expr expr = primary();

        for (;;) {
            if (match(Token.Type.Dot)) {
                Token name = consume(Token.Type.Identifier, "Expected property name after '.'.");
                expr = new Expr.Get(line, expr, name);
            } else if (match(Token.Type.LeftParen)) {
                ArrayList<Expr> args = new ArrayList<>();
                if (!check(Token.Type.RightParen)) {
                    args.add(expression());
                    while (match(Token.Type.Comma)) args.add(expression());
                }
                consume(Token.Type.RightParen, "Expected ')' after arguments.");
                expr = new Expr.Call(line, expr, args);
            } else break;
        }

        return expr;
    }

    private Expr primary() {
        int line = current.line;
        switch (current.type) {
            case Nil:        advance(); return new Expr.Literal(line, null);
            case True:       advance(); return new Expr.Literal(line, true);
            case False:      advance(); return new Expr.Literal(line, false);
            case Number:     advance(); return new Expr.Literal(line, Double.parseDouble(previous.lexeme));
            case String:     advance(); return new Expr.Literal(line, previous.lexeme.substring(1, previous.lexeme.length() - 1));
            case Identifier: advance(); return new Expr.Variable(line, previous);
            case Self:       advance(); return new Expr.Self(line);
            case LeftBrace:  advance(); return tableExpression(line);
            case Function:   advance(); return functionExpression(line);
            case LeftParen: {
                advance();
                Expr expr = expression();
                consume(Token.Type.RightParen, "Expected ')' after expression.");
                return new Expr.Grouping(line, expr);
            }
            default:
                error(next, "Expected expression.");
                return null;
        }
    }

    private Expr tableExpression(int line) {
        HashMap<Token, Expr> values = new HashMap<>(2);

        while (!check(Token.Type.RightBrace)) {
            Token key = current;
            advance();
            consume(Token.Type.Colon, "Expected ':' after value key.");
            values.put(key, expression());
            if (!check(Token.Type.RightBrace)) consume(Token.Type.Comma, "Expected ',' before next value.");
        }

        consume(Token.Type.RightBrace, "Expected '}' at the end of an object.");
        return new Expr.Table(line, values);
    }

    private Expr functionExpression(int line) {
        ArrayList<Token> args = new ArrayList<>();
        ArrayList<Stmt> stmts = new ArrayList<>();

        consume(Token.Type.LeftParen, "Expected '(' before function parameters.");
        while (!check(Token.Type.RightParen)) {
            args.add(current);
            advance();
            if (!check(Token.Type.RightParen)) consume(Token.Type.Comma, "Expected ',' before next parameter.");
        }
        consume(Token.Type.RightParen, "Expected ')' after function parameters.");

        consume(Token.Type.LeftBrace, "Expected '{' before function body.");
        while (!check(Token.Type.RightBrace) && !isAtEnd()) stmts.add(declaration());
        consume(Token.Type.RightBrace, "Expected '}' after function body.");

        return new Expr.Function(line, args, stmts);
    }

    // Helper methods

    private Stmt blockOrStatement() {
        if (match(Token.Type.LeftBrace)) return blockStatement();
        return statement();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous.type == Token.Type.Semicolon) return;

            switch (next.type) {
                case Function:
                case Var:
                case For:
                case If:
                case While:
                case Return:
                case Break:
                case Continue: return;
            }

            advance();
        }
    }

    private boolean isAtEnd() {
        return current.type == Token.Type.Eof;
    }
    private Token advance() {
        previous = current;
        current = next;
        next = lexer.scanToken();
        if (next.type == Token.Type.Error) error(next, next.lexeme);
        return previous;
    }
    private boolean check(Token.Type type) {
        return current.type == type;
    }
    private Token consume(Token.Type type, String message) {
        if (check(type)) return advance();
        error(next, message);
        return null;
    }
    private boolean match(Token.Type ...types) {
        for (int i = 0; i < types.length; i++) {
            if (check(types[i])) {
                advance();
                return true;
            }
        }

        return false;
    }

    private void error(Token token, String message) {
        if (token.type == Token.Type.Eof) result.errors.add(new Error(token.line, "end", message));
        else result.errors.add(new Error(token.line, "'" + token.lexeme + "'", message));
        throw new ParseError();
    }
}
