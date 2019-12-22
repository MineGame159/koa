package minegame159.koa.ast;

import minegame159.koa.Token;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Expr {
    public static interface Visitor {
        public void visitBinaryExpr(Binary expr);
        public void visitGroupingExpr(Grouping expr);
        public void visitLiteralExpr(Literal expr);
        public void visitLogicalExpr(Logical expr);
        public void visitUnaryExpr(Unary expr);
        public void visitVariableExpr(Variable expr);
        public void visitAssignExpr(Assign expr);
        public void visitTableExpr(Table expr);
        public void visitGetExpr(Get expr);
        public void visitSetExpr(Set expr);
        public void visitCallExpr(Call expr);
        public void visitFunctionExpr(Function expr);
        public void visitSelfExpr(Self expr);
    }

    public static class Binary extends Expr {
        public Expr left;
        public Token operator;
        public Expr right;

        public Binary(int line, Expr left, Token operator, Expr right) {
            super(line);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBinaryExpr(this);
        }
    }

    public static class Grouping extends Expr {
        public Expr expr;

        public Grouping(int line, Expr expr) {
            super(line);
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGroupingExpr(this);
        }
    }

    public static class Literal extends Expr {
        public java.lang.Object value;

        public Literal(int line, java.lang.Object value) {
            super(line);
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLiteralExpr(this);
        }
    }

    public static class Logical extends Expr {
        public Expr left;
        public Token operator;
        public Expr right;

        public Logical(int line, Expr left, Token operator, Expr right) {
            super(line);
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLogicalExpr(this);
        }
    }

    public static class Unary extends Expr {
        public Token operator;
        public Expr right;

        public Unary(int line, Token operator, Expr right) {
            super(line);
            this.operator = operator;
            this.right = right;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitUnaryExpr(this);
        }
    }

    public static class Variable extends Expr {
        public Token name;

        public Variable(int line, Token name) {
            super(line);
            this.name = name;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitVariableExpr(this);
        }
    }

    public static class Assign extends Expr {
        public Token name;
        public Token operator;
        public Expr value;

        public Assign(int line, Token name, Token operator, Expr value) {
            super(line);
            this.name = name;
            this.operator = operator;
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssignExpr(this);
        }
    }

    public static class Table extends Expr {
        public HashMap<Token, Expr> values;

        public Table(int line, HashMap<Token, Expr> values) {
            super(line);
            this.values = values;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitTableExpr(this);
        }
    }

    public static class Get extends Expr {
        public Expr object;
        public Token name;
        public Expr key;

        public Get(int line, Expr object, Token name) {
            super(line);
            this.object = object;
            this.name = name;
        }

        public Get(int line, Expr object, Expr key) {
            super(line);
            this.object = object;
            this.key = key;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitGetExpr(this);
        }
    }

    public static class Set extends Expr {
        public Expr object;
        public Token name;
        public Expr key;
        public Token operator;
        public Expr value;

        public Set(int line, Expr object, Token name, Token operator, Expr value) {
            super(line);
            this.object = object;
            this.name = name;
            this.operator = operator;
            this.value = value;
        }

        public Set(int line, Expr object, Expr key, Token operator, Expr value) {
            super(line);
            this.object = object;
            this.key = key;
            this.operator = operator;
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitSetExpr(this);
        }
    }

    public static class Call extends Expr {
        public Expr callee;
        public ArrayList<Expr> args;

        public Call(int line, Expr callee, ArrayList<Expr> args) {
            super(line);
            this.callee = callee;
            this.args = args;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCallExpr(this);
        }
    }

    public static class Function extends Expr {
        public ArrayList<Token> args;
        public ArrayList<Stmt> stmts;

        public Function(int line, ArrayList<Token> args, ArrayList<Stmt> stmts) {
            super(line);
            this.args = args;
            this.stmts = stmts;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitFunctionExpr(this);
        }
    }

    public static class Self extends Expr {
        public Self(int line) {
            super(line);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitSelfExpr(this);
        }
    }

    public int line;

    public Expr(int line) {
        this.line = line;
    }

    public abstract void accept(Visitor visitor);
}
