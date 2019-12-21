package minegame159.koa.ast;

import minegame159.koa.Error;
import minegame159.koa.Warning;

import java.util.ArrayList;
import java.util.HashMap;

public class Validator implements Stmt.Visitor, Expr.Visitor {
    public static class Result {
        public ArrayList<Error> errors = new ArrayList<>(0);
        public ArrayList<Warning> warnings = new ArrayList<>(0);

        public boolean hadError() {
            return errors.size() > 0;
        }
        public void printErrors() {
            for (int i = 0; i < errors.size(); i++) System.out.println(errors.get(i));
        }
        public void printWarnings() {
            for (int i = 0; i < warnings.size(); i++) System.out.println(warnings.get(i));
        }
    }

    private Result result = new Result();
    private HashMap<String, Variable> definedVariables = new HashMap<>();
    private boolean insideLoop = false;
    private boolean insideFunction = false;

    private Validator() {}

    public static Result validate(ArrayList<Stmt> stmts) {
        Validator validator = new Validator();
        for (int i = 0; i < stmts.size(); i++) validator.validate(stmts.get(i));
        return validator.result;
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        validate(stmt.value);

        if (!insideFunction) error(new Error(stmt.line, "Return statement can only be used inside a function."));
    }

    // Statements

    @Override
    public void visitBlockStmt(Stmt.Block stmt) {
        for (int i = 0; i < stmt.stmts.size(); i++) validate(stmt.stmts.get(i));
    }

    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        validate(stmt.expr);
    }

    @Override
    public void visitVarStmt(Stmt.Var stmt) {
        if (!definedVariables.containsKey(stmt.name.lexeme)) definedVariables.put(stmt.name.lexeme, new Variable(false, false));
        if (stmt.initializer != null) validate(stmt.initializer);
        definedVariables.put(stmt.name.lexeme, new Variable(true, stmt.initializer instanceof Expr.Function));
    }

    @Override
    public void visitIfStmt(Stmt.If stmt) {
        validate(stmt.condition);
        validate(stmt.thenBranch);
        validate(stmt.elseBranch);

        if (stmt.thenBranch instanceof Stmt.Block && ((Stmt.Block) stmt.thenBranch).stmts.size() == 0) warning(new Warning(stmt.thenBranch.line, "Then branch in if statement has empty body."));
        if (stmt.elseBranch instanceof Stmt.Block && ((Stmt.Block) stmt.elseBranch).stmts.size() == 0) warning(new Warning(stmt.elseBranch.line, "Else branch in if statement has empty body."));
    }

    @Override
    public void visitWhileStmt(Stmt.While stmt) {
        validate(stmt.condition);
        boolean enclosingInsideLoop = insideLoop;
        insideLoop = true;
        validate(stmt.body);
        insideLoop = enclosingInsideLoop;

        if (stmt.body instanceof Stmt.Block && ((Stmt.Block) stmt.body).stmts.size() == 0) warning(new Warning(stmt.body.line, "While loop has empty body."));
    }

    @Override
    public void visitForStmt(Stmt.For stmt) {
        validate(stmt.initializer);
        validate(stmt.condition);
        validate(stmt.increment);
        boolean enclosingInsideLoop = insideLoop;
        insideLoop = true;
        validate(stmt.body);
        insideLoop = enclosingInsideLoop;

        if (stmt.body instanceof Stmt.Block && ((Stmt.Block) stmt.body).stmts.size() == 0) warning(new Warning(stmt.body.line, "For loop has empty body."));
    }

    @Override
    public void visitBreakStmt(Stmt.Break stmt) {
        if (!insideLoop) error(new Error(stmt.line, "Break statement can only be used inside a loop."));
    }

    @Override
    public void visitContinueStmt(Stmt.Continue stmt) {
        if (!insideLoop) error(new Error(stmt.line, "Continue statement can only be used inside a loop."));
    }

    @Override
    public void visitAssignExpr(Expr.Assign expr) {
        validate(expr.value);
    }

    @Override
    public void visitTableExpr(Expr.Table expr) {
        for (Expr e : expr.values.values()) validate(e);
    }

    @Override
    public void visitGetExpr(Expr.Get expr) {
        validate(expr.object);
    }

    @Override
    public void visitSetExpr(Expr.Set expr) {
        validate(expr.object);
        validate(expr.value);
    }

    // Expressions

    @Override
    public void visitBinaryExpr(Expr.Binary expr) {
        validate(expr.left);
        validate(expr.right);
    }

    @Override
    public void visitGroupingExpr(Expr.Grouping expr) {
        validate(expr.expr);
    }

    @Override
    public void visitLiteralExpr(Expr.Literal expr) {

    }

    @Override
    public void visitLogicalExpr(Expr.Logical expr) {
        validate(expr.left);
        validate(expr.right);
    }

    @Override
    public void visitUnaryExpr(Expr.Unary expr) {
        validate(expr.right);
    }

    @Override
    public void visitVariableExpr(Expr.Variable expr) {
        //Variable var = definedVariables.get(expr.name.lexeme);
        //if (var != null && !var.defined && !var.function) error(new Error(expr.line, "Variable cannot be accessed in its own initializer."));
    }

    @Override
    public void visitCallExpr(Expr.Call expr) {
        validate(expr.callee);
        for (int i = 0; i < expr.args.size(); i++) validate(expr.args.get(i));
    }

    @Override
    public void visitFunctionExpr(Expr.Function expr) {
        boolean enclosingInsideFunction = insideFunction;
        insideFunction = true;
        for (int i = 0; i < expr.stmts.size(); i++) validate(expr.stmts.get(i));
        insideFunction = enclosingInsideFunction;

        if ((expr.stmts.size() > 0 && !(expr.stmts.get(expr.stmts.size() - 1) instanceof Stmt.Return)) || expr.stmts.size() == 0) {
            int line = 0;
            if (expr.stmts.size() > 0) line = expr.stmts.get(expr.stmts.size() - 1).line;
            expr.stmts.add(new Stmt.Return(line, new Expr.Literal(line, null)));
        }
    }

    @Override
    public void visitSelfExpr(Expr.Self expr) {

    }

    // Helper methods

    private void validate(Stmt stmt) {
        if (stmt != null) stmt.accept(this);
    }
    private void validate(Expr expr) {
        if (expr != null) expr.accept(this);
    }

    private void error(Error error) {
        result.errors.add(error);
    }
    private void warning(Warning warning) {
        result.warnings.add(warning);
    }

    private static class Variable {
        public boolean defined;
        public final boolean function;

        public Variable(boolean defined, boolean function) {
            this.defined = defined;
            this.function = function;
        }
    }
}
