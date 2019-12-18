package minegame159.koa.ast;

import minegame159.koa.Token;

import java.util.ArrayList;

public abstract class Stmt {
    public static interface Visitor {
        public void visitBlockStmt(Block stmt);
        public void visitExpressionStmt(Expression stmt);
        public void visitVarStmt(Var stmt);
        public void visitIfStmt(If stmt);
        public void visitWhileStmt(While stmt);
        public void visitForStmt(For stmt);
        public void visitBreakStmt(Break stmt);
        public void visitContinueStmt(Continue stmt);
        public void visitReturnStmt(Return stmt);
    }

    public static class Block extends Stmt {
        public ArrayList<Stmt> stmts;

        public Block(int line, ArrayList<Stmt> stmts) {
            super(line);
            this.stmts = stmts;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBlockStmt(this);
        }
    }

    public static class Expression extends Stmt {
        public Expr expr;

        public Expression(int line, Expr expr) {
            super(line);
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitExpressionStmt(this);
        }
    }

    public static class Var extends Stmt {
        public Token name;
        public Expr initializer;

        public Var(int line, Token name, Expr initializer) {
            super(line);
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitVarStmt(this);
        }
    }

    public static class If extends Stmt {
        public Expr condition;
        public Stmt thenBranch;
        public Stmt elseBranch;

        public If(int line, Expr condition, Stmt thenBranch, Stmt elseBranch) {
            super(line);
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitIfStmt(this);
        }
    }

    public static class While extends Stmt {
        public Expr condition;
        public Stmt body;

        public While(int line, Expr condition, Stmt body) {
            super(line);
            this.condition = condition;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitWhileStmt(this);
        }
    }

    public static class For extends Stmt {
        public Stmt initializer;
        public Expr condition;
        public Expr increment;
        public Stmt body;

        public For(int line, Stmt initializer, Expr condition, Expr increment, Stmt body) {
            super(line);
            this.initializer = initializer;
            this.condition = condition;
            this.increment = increment;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitForStmt(this);
        }
    }

    public static class Break extends Stmt {
        public Break(int line) {
            super(line);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBreakStmt(this);
        }
    }

    public static class Continue extends Stmt {
        public Continue(int line) {
            super(line);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitContinueStmt(this);
        }
    }

    public static class Return extends Stmt {
        public Expr value;

        public Return(int line, Expr value) {
            super(line);
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitReturnStmt(this);
        }
    }

    public int line;

    public Stmt(int line) {
        this.line = line;
    }

    public abstract void accept(Visitor visitor);
}
