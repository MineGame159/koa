package minegame159.koa;

import minegame159.koa.asm.ClassBuilder;
import minegame159.koa.asm.MethodBuilder;
import minegame159.koa.ast.Expr;
import minegame159.koa.ast.Stmt;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

import static minegame159.koa.asm.ASM.*;

public class Compiler implements Stmt.Visitor, Expr.Visitor {
    private ClassBuilder c;
    private MethodBuilder m;

    private int scopeDepth;
    private ArrayList<Local> locals = new ArrayList<>();

    private Label lLoopEnd, lLoopExit;
    private int line;
    private int functionCount;

    private Compiler(String className, ArrayList<Stmt> stmts) {
        c = new ClassBuilder("minegame159/koa/compiled/" + className, OBJECT, new String[] {RUNNABLE});
        c.field("globals", GLOBALS_D);
        {   // constructor
            MethodBuilder m = c.method("<init>", "V");
            m.callSuper();
            m.insn(Opcodes.RETURN);
            m.end();
        }
        m = c.method("run", "V");
        for (int i = 0; i < stmts.size(); i++) compile(stmts.get(i));
        m.insn(Opcodes.RETURN);
        m.end();
        c.end();
    }

    public static Runnable compile(ArrayList<Stmt> stmts, Globals globals) {
        String className = "Sel" + (int) (Math.random() * 1000000000);
        Compiler compiler = new Compiler(className, stmts);
        Class klass = KoaClassLoader.instance.define(compiler.c.name.replace('/', '.'), compiler.c.build());
        try {
            Runnable obj = (Runnable) klass.newInstance();
            klass.getField("globals").set(obj, globals);
            return obj;
        } catch (InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Statements

    @Override
    public void visitBlockStmt(Stmt.Block stmt) {
        beginScope();
        for (int i = 0; i < stmt.stmts.size(); i++) compile(stmt.stmts.get(i));
        endScope();
    }

    @Override
    public void visitExpressionStmt(Stmt.Expression stmt) {
        compile(stmt.expr);
        m.insn(Opcodes.POP);
    }

    @Override
    public void visitVarStmt(Stmt.Var stmt) {
        compile(stmt.initializer);
        if (stmt.initializer == null) emitNull();

        if (scopeDepth == 0) {
            m.ldcInsn(stmt.name.lexeme);
            m.insn(Opcodes.SWAP);
            setGlobal();
        } else setLocal(addLocal(stmt.name));
    }

    @Override
    public void visitIfStmt(Stmt.If stmt) {
        Label lElse = new Label();
        Label lExit = new Label();

        // Condition
        compile(stmt.condition);
        isTruthy();
        m.jumpInsn(Opcodes.IFEQ, lElse);

        // Then
        compile(stmt.thenBranch);
        m.jumpInsn(Opcodes.GOTO, lExit);

        // Else
        m.label(lElse);
        compile(stmt.elseBranch);

        // Exit
        m.label(lExit);
    }

    @Override
    public void visitWhileStmt(Stmt.While stmt) {
        Label lLoopStart = new Label();
        lLoopEnd = new Label();
        lLoopExit = new Label();

        // Loop start (condition)
        m.label(lLoopStart);
        compile(stmt.condition);
        isTruthy();
        m.jumpInsn(Opcodes.IFEQ, lLoopExit);

        // Body
        compile(stmt.body);
        m.label(lLoopEnd);
        m.jumpInsn(Opcodes.GOTO, lLoopStart);

        // Exit
        m.label(lLoopExit);
    }

    @Override
    public void visitForStmt(Stmt.For stmt) {
        Label lLoopStart = new Label();
        lLoopEnd = new Label();
        lLoopExit = new Label();

        beginScope();

        // Initializer
        compile(stmt.initializer);

        // Loop start (condition)
        m.label(lLoopStart);
        compile(stmt.condition);
        if (stmt.condition != null) {
            isTruthy();
            m.jumpInsn(Opcodes.IFEQ, lLoopExit);
        }

        // Body
        compile(stmt.body);
        m.label(lLoopEnd);
        compile(stmt.increment);
        if (stmt.increment != null) m.insn(Opcodes.POP);
        m.jumpInsn(Opcodes.GOTO, lLoopStart);

        // Exit
        m.label(lLoopExit);

        endScope();
    }

    @Override
    public void visitBreakStmt(Stmt.Break stmt) {
        m.jumpInsn(Opcodes.GOTO, lLoopExit);
    }

    @Override
    public void visitContinueStmt(Stmt.Continue stmt) {
        m.jumpInsn(Opcodes.GOTO, lLoopEnd);
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        compile(stmt.value);
        m.insn(Opcodes.ARETURN);
    }

    // Expressions

    @Override
    public void visitBinaryExpr(Expr.Binary expr) {
        compile(expr.left);

        if (expr.operator.type == Token.Type.EqualEqual || expr.operator.type == Token.Type.BangEqual) {
            compile(expr.right);
            valueEquals();
            if (expr.operator.type == Token.Type.BangEqual) negateBoolean();
            emitBool();
        } else if (expr.operator.type == Token.Type.Greater || expr.operator.type == Token.Type.GreaterEqual || expr.operator.type == Token.Type.Less || expr.operator.type == Token.Type.LessEqual) {
            Label lFalse = new Label();
            Label lExit = new Label();

            toNumber();
            compile(expr.right);
            toNumber();
            m.insn(Opcodes.DCMPL);

            switch (expr.operator.type) {
                case Greater:      m.jumpInsn(Opcodes.IFLE, lFalse); break;
                case GreaterEqual: m.jumpInsn(Opcodes.IFLT, lFalse); break;
                case Less:         m.jumpInsn(Opcodes.IFGE, lFalse); break;
                case LessEqual:    m.jumpInsn(Opcodes.IFGT, lFalse); break;
            }
            m.insn(Opcodes.ICONST_1);
            m.jumpInsn(Opcodes.GOTO, lExit);

            m.label(lFalse);
            m.insn(Opcodes.ICONST_0);

            m.label(lExit);
            emitBool();
        } else {
            toNumber();
            compile(expr.right);
            toNumber();

            switch (expr.operator.type) {
                case Plus:       m.insn(Opcodes.DADD); break;
                case Minus:      m.insn(Opcodes.DSUB); break;
                case Star:       m.insn(Opcodes.DMUL); break;
                case Slash:      m.insn(Opcodes.DDIV); break;
                case Percentage: m.insn(Opcodes.DREM); break;
            }

            emitNumber();
        }
    }

    @Override
    public void visitGroupingExpr(Expr.Grouping expr) {
        compile(expr.expr);
    }

    @Override
    public void visitLiteralExpr(Expr.Literal expr) {
        if (expr.value == null) emitNull();
        else if (expr.value instanceof Boolean) emitBool((boolean) expr.value);
        else if (expr.value instanceof Double) emitNumber((double) expr.value);
        else if (expr.value instanceof String) {
            m.typeInsn(Opcodes.NEW, STRING_TABLE);
            m.insn(Opcodes.DUP);
            m.ldcInsn(expr.value);
            m.methodInsnSpecial(STRING_TABLE, "<init>", STRING_D, "V");
        }
    }

    @Override
    public void visitLogicalExpr(Expr.Logical expr) {
        Label lExit = new Label();

        compile(expr.left);
        isTruthy();
        m.insn(Opcodes.DUP);

        if (expr.operator.type == Token.Type.And) m.jumpInsn(Opcodes.IFEQ, lExit);
        else if (expr.operator.type == Token.Type.Or) m.jumpInsn(Opcodes.IFGT, lExit);

        m.insn(Opcodes.POP);
        compile(expr.right);
        isTruthy();

        m.label(lExit);
        emitBool();
    }

    @Override
    public void visitUnaryExpr(Expr.Unary expr) {
        compile(expr.right);

        if (expr.operator.type == Token.Type.Minus) {
            toNumber();
            m.insn(Opcodes.DNEG);
            emitNumber();
        } else if (expr.operator.type == Token.Type.Bang) {
            isTruthy();
            negateBoolean();
            emitBool();
        }
    }

    @Override
    public void visitVariableExpr(Expr.Variable expr) {
        Local local = resolveLocal(expr.name);

        if (local != null) getLocal(local);
        else {
            m.ldcInsn(expr.name.lexeme);
            getGlobal();
        }
    }

    @Override
    public void visitAssignExpr(Expr.Assign expr) {
        Local local = resolveLocal(expr.name);

        if (expr.operator.type == Token.Type.Equal) { // =
            compile(expr.value);
            m.insn(Opcodes.DUP);

            if (local != null) setLocal(local);
            else {
                m.ldcInsn(expr.name.lexeme);
                m.insn(Opcodes.SWAP);
                setGlobal();
            }
        } else { // ++, --, +=, -=, *=, /=, %=
            // Get value
            if (local != null) getLocal(local);
            else {
                m.ldcInsn(expr.name.lexeme);
                getGlobal();
            }

            // Compute value
            if (expr.operator.type == Token.Type.PlusPlus || expr.operator.type == Token.Type.MinusMinus) { // ++, --
                toNumber();
                m.insn(Opcodes.DCONST_1);
                m.insn(expr.operator.type == Token.Type.PlusPlus ? Opcodes.DADD : Opcodes.DSUB);
                emitNumber();
            } else { // +=, -=, *=, /=, %=
                toNumber();
                compile(expr.value);
                toNumber();

                switch (expr.operator.type) {
                    case PlusEqual:       m.insn(Opcodes.DADD); break;
                    case MinusEqual:      m.insn(Opcodes.DSUB); break;
                    case StarEqual:       m.insn(Opcodes.DMUL); break;
                    case SlashEqual:      m.insn(Opcodes.DDIV); break;
                    case PercentageEqual: m.insn(Opcodes.DREM); break;
                }

                emitNumber();
            }

            // Set value
            m.insn(Opcodes.DUP);
            if (local != null) setLocal(local);
            else {
                m.ldcInsn(expr.name.lexeme);
                m.insn(Opcodes.SWAP);
                setGlobal();
            }
        }
    }

    @Override
    public void visitTableExpr(Expr.Table expr) {
        // Create new table
        m.typeInsn(Opcodes.NEW, VALUE_TABLE);
        m.insn(Opcodes.DUP);
        m.methodInsnSpecial(VALUE_TABLE, "<init>", "V");

        // Set values
        for (Token key : expr.values.keySet()) {
            m.insn(Opcodes.DUP);
            m.ldcInsn(key.lexeme);
            compile(expr.values.get(key));
            tableSet();
        }
    }

    @Override
    public void visitGetExpr(Expr.Get expr) {
        compile(expr.object);
        toTable();

        // Set last table
        m.insn(Opcodes.DUP);
        m.varInsn(Opcodes.ALOAD, 0);
        m.fieldInsn(Opcodes.GETFIELD, c.name, "globals", GLOBALS_D);
        m.insn(Opcodes.SWAP);
        m.fieldInsn(Opcodes.PUTFIELD, GLOBALS, "lastTable", VALUE_TABLE_D);

        m.ldcInsn(expr.name.lexeme);
        tableGet();
    }

    @Override
    public void visitSetExpr(Expr.Set expr) {
        compile(expr.object);
        toTable();
        m.insn(Opcodes.DUP);

        if (expr.operator.type == Token.Type.Equal) { // =
            m.ldcInsn(expr.name.lexeme);
            compile(expr.value);
            tableSet();
        } else { // ++, --, +=, -=, *=, /=, %=
            // Get value
            m.insn(Opcodes.DUP);
            m.ldcInsn(expr.name.lexeme);
            tableGet();

            // Compute value
            if (expr.operator.type == Token.Type.PlusPlus || expr.operator.type == Token.Type.MinusMinus) { // ++, --
                toNumber();
                m.insn(Opcodes.DCONST_1);
                m.insn(expr.operator.type == Token.Type.PlusPlus ? Opcodes.DADD : Opcodes.DSUB);
                emitNumber();
            } else { // +=, -=, *=, /=, %=
                toNumber();
                compile(expr.value);
                toNumber();

                switch (expr.operator.type) {
                    case PlusEqual:       m.insn(Opcodes.DADD); break;
                    case MinusEqual:      m.insn(Opcodes.DSUB); break;
                    case StarEqual:       m.insn(Opcodes.DMUL); break;
                    case SlashEqual:      m.insn(Opcodes.DDIV); break;
                    case PercentageEqual: m.insn(Opcodes.DREM); break;
                }

                emitNumber();
            }

            // Set value
            m.ldcInsn(expr.name.lexeme);
            m.insn(Opcodes.SWAP);
            tableSet();
        }
    }

    @Override
    public void visitCallExpr(Expr.Call expr) {
        Label lOk = new Label();

        compile(expr.callee);
        toFunction(); // function

        // Check if var arg
        m.insn(Opcodes.DUP); // function, function
        m.methodInsn(VALUE_FUNCTION, "argCount", "I"); // function, functionArgCount
        m.jumpInsn(Opcodes.IFLT, lOk); // function

        // Check argument count
        m.insn(Opcodes.DUP); // function, function
        m.ldcInsn(expr.args.size()); // function, function, argCount
        m.insn(Opcodes.SWAP); // function, argCount, function
        m.methodInsn(VALUE_FUNCTION, "argCount", "I"); // function, argCount, functionArgCount
        m.jumpInsn(Opcodes.IF_ICMPEQ, lOk);
        m.methodInsn(VALUE_FUNCTION, "argCount", "I");
        throwWrongNumberOfArgumentsException(expr.args.size());

        // Prepare arguments
        m.label(lOk);
        m.ldcInsn(expr.args.size());
        m.typeInsn(Opcodes.ANEWARRAY, VALUE);
        for (int i = 0; i < expr.args.size(); i++) {
            m.insn(Opcodes.DUP);
            m.ldcInsn(i);
            compile(expr.args.get(i));
            m.insn(Opcodes.AASTORE);
        }

        // Call function
        m.methodInsn(VALUE_FUNCTION, "run", "[" + VALUE_D, VALUE_D);
    }

    @Override
    public void visitFunctionExpr(Expr.Function expr) {
        ClassBuilder enclosingC = c;
        c = new ClassBuilder(c.name + "Function" + functionCount, VALUE_FUNCTION, null);

        c.field("globals", GLOBALS_D);
        {   // constructor
            MethodBuilder m = c.method("<init>", "V");
            m.callSuper();
            m.insn(Opcodes.RETURN);
            m.end();
        }
        {   // int argCount()
            MethodBuilder m = c.method("argCount", "I");
            m.ldcInsn(expr.args.size());
            m.insn(Opcodes.IRETURN);
            m.end();
        }
        {   // Value run(Value[] args)
            ArrayList<Local> enclosingLocals = locals;
            locals = new ArrayList<>();
            MethodBuilder enclosingM = m;
            m = c.method("run", "[" + VALUE_D, VALUE_D);
            for (int i = 0; i < expr.args.size(); i++) addLocalFunctionArgument(expr.args.get(i), i);
            for (int i = 0; i < expr.stmts.size(); i++) compile(expr.stmts.get(i));
            emitNull();
            m.end();
            m = enclosingM;
            locals = enclosingLocals;
        }
        c.end();
        functionCount++;

        KoaClassLoader.instance.define(c.name.replace('/', '.'), c.build());
        m.typeInsn(Opcodes.NEW, c.name);
        m.insn(Opcodes.DUP);
        m.methodInsnSpecial(c.name, "<init>", "V");
        m.insn(Opcodes.DUP);
        m.varInsn(Opcodes.ALOAD, 0);
        m.fieldInsn(Opcodes.GETFIELD, enclosingC.name, "globals", GLOBALS_D);
        m.fieldInsn(Opcodes.PUTFIELD, c.name, "globals", GLOBALS_D);

        c = enclosingC;
    }

    @Override
    public void visitSelfExpr(Expr.Self expr) {
        m.varInsn(Opcodes.ALOAD, 0);
        m.fieldInsn(Opcodes.GETFIELD, c.name, "globals", GLOBALS_D);
        m.fieldInsn(Opcodes.GETFIELD, GLOBALS, "lastTable", VALUE_TABLE_D);
    }

    // Helper methods

    private Local addLocal(Token name) {
        int index;
        if (locals.size() == 0) index = 4;
        else index = locals.get(locals.size() - 1).index + 1;
        Local local = new Local(name, scopeDepth, index, false);
        locals.add(local);
        return local;
    }
    private void addLocalFunctionArgument(Token name, int i) {
        Local local = new Local(name, scopeDepth, i, true);
        locals.add(local);
    }
    private void setLocal(Local local) {
        if (!local.functionArgument) m.varInsn(Opcodes.ASTORE, local.index);
        else {
            m.varInsn(Opcodes.ALOAD, 1); // value, array
            m.insn(Opcodes.SWAP); // array, value
            m.ldcInsn(local.index); // array, value, index
            m.insn(Opcodes.SWAP); // array, index, value
            m.insn(Opcodes.AASTORE); // -/-
        }
    }
    private void getLocal(Local local) {
        if (!local.functionArgument) m.varInsn(Opcodes.ALOAD, local.index);
        else {
            m.varInsn(Opcodes.ALOAD, 1);
            m.ldcInsn(local.index);
            m.insn(Opcodes.AALOAD);
        }
    }
    private Local resolveLocal(Token name) {
        for (int i = locals.size() - 1; i >= 0; i--) {
            Local local = locals.get(i);
            if (local.name.lexeme.equals(name.lexeme)) return local;
        }

        return null;
    }

    private void setGlobal() { // key, value
        m.varInsn(Opcodes.ASTORE, 2); // key
        m.varInsn(Opcodes.ALOAD, 0); // key, this
        m.fieldInsn(Opcodes.GETFIELD, c.name, "globals", GLOBALS_D); // key, globals
        m.insn(Opcodes.SWAP); // globals, key
        m.varInsn(Opcodes.ALOAD, 2); // globals, key, value
        m.methodInsn(GLOBALS, "set", STRING_D, VALUE_D, "V"); // -/-
    }
    private void getGlobal() { // key
        m.varInsn(Opcodes.ALOAD, 0); // key, this
        m.fieldInsn(Opcodes.GETFIELD, c.name, "globals", GLOBALS_D); // key, globals
        m.insn(Opcodes.SWAP); // globals, key
        m.methodInsn(GLOBALS, "get", STRING_D, VALUE_D); // value
    }

    private void emitNull() {
        m.fieldInsn(Opcodes.GETSTATIC, VALUE, "NULL", VALUE_NULL_D);
    }

    private void emitBool(boolean value) {
        m.typeInsn(Opcodes.NEW, VALUE_BOOL);
        m.insn(Opcodes.DUP);
        m.ldcInsn(value);
        m.methodInsnSpecial(VALUE_BOOL, "<init>", "Z", "V");
    }
    private void emitBool() {
        m.varInsn(Opcodes.ISTORE, 2);
        m.typeInsn(Opcodes.NEW, VALUE_BOOL);
        m.insn(Opcodes.DUP);
        m.varInsn(Opcodes.ILOAD, 2);
        m.methodInsnSpecial(VALUE_BOOL, "<init>", "Z", "V");
    }

    private void emitNumber(double value) {
        m.typeInsn(Opcodes.NEW, VALUE_NUMBER);
        m.insn(Opcodes.DUP);
        m.ldcInsn(value);
        m.methodInsnSpecial(VALUE_NUMBER, "<init>", "D", "V");
    }
    private void emitNumber() {
        m.varInsn(Opcodes.DSTORE, 2);
        m.typeInsn(Opcodes.NEW, VALUE_NUMBER);
        m.insn(Opcodes.DUP);
        m.varInsn(Opcodes.DLOAD, 2);
        m.methodInsnSpecial(VALUE_NUMBER, "<init>", "D", "V");
    }

    private void isNumber() {
        m.methodInsn(VALUE, "isNumber", "Z");
    }
    private void isTable() {
        m.methodInsn(VALUE, "isTable", "Z");
    }
    private void isFunction() {
        m.methodInsn(VALUE, "isFunction", "Z");
    }

    private void toNumber() {
        Label lOk = new Label();

        m.insn(Opcodes.DUP);
        isNumber();
        m.jumpInsn(Opcodes.IFGT, lOk);
        throwWrongTypeException();

        m.label(lOk);
        m.methodInsn(VALUE, "toNumber", "D");
    }
    private void toTable() {
        Label lOk = new Label();

        m.insn(Opcodes.DUP);
        isTable();
        m.jumpInsn(Opcodes.IFGT, lOk);
        throwWrongTypeException();

        m.label(lOk);
        m.methodInsn(VALUE, "toTable", VALUE_TABLE_D);
    }
    private void toFunction() {
        Label lOk = new Label();

        m.insn(Opcodes.DUP);
        isFunction();
        m.jumpInsn(Opcodes.IFGT, lOk);
        throwWrongTypeException();

        m.label(lOk);
        m.methodInsn(VALUE, "toFunction", VALUE_FUNCTION_D);
    }

    private void isTruthy() {
        m.methodInsn(VALUE, "isTruthy", "Z");
    }
    private void valueEquals() {
        m.methodInsn(VALUE, "equals", OBJECT_D, "Z");
    }

    private void tableSet() {
        m.methodInsn(VALUE_TABLE, "set", STRING_D, VALUE_D, "V");
    }
    private void tableGet() {
        m.methodInsn(VALUE_TABLE, "getOrNull", STRING_D, VALUE_D);
    }

    private void negateBoolean() {
        Label lFalse = new Label();
        Label lExit = new Label();

        m.jumpInsn(Opcodes.IFNE, lFalse);
        m.insn(Opcodes.ICONST_1);
        m.jumpInsn(Opcodes.GOTO, lExit);

        m.label(lFalse);
        m.insn(Opcodes.ICONST_0);

        m.label(lExit);
    }

    private void throwWrongTypeException() {
        m.typeInsn(Opcodes.NEW, WRONGTYPEEXCEPTION);
        m.insn(Opcodes.DUP);
        m.ldcInsn(line);
        m.methodInsnSpecial(WRONGTYPEEXCEPTION, "<init>", "I", "V");
        m.insn(Opcodes.ATHROW);
    }
    private void throwWrongNumberOfArgumentsException(int got) {
        m.varInsn(Opcodes.ISTORE, 2);
        m.typeInsn(Opcodes.NEW, WRONGNUMBEROFARGUENTSEXCEPTION); // exception
        m.insn(Opcodes.DUP); // exception, exception
        m.ldcInsn(line); // exception, exception, line
        m.varInsn(Opcodes.ILOAD, 2);
        m.ldcInsn(got);
        m.methodInsnSpecial(WRONGNUMBEROFARGUENTSEXCEPTION, "<init>", "I", "I", "I", "V"); // exception
        m.insn(Opcodes.ATHROW); // -/-
    }

    private void beginScope() {
        scopeDepth++;
    }
    private void endScope() {
        scopeDepth--;
        while (locals.size() > 0 && locals.get(locals.size() - 1).scopeDepth > scopeDepth) locals.remove(locals.size() - 1);
    }

    private void compile(Stmt stmt) {
        if (stmt != null) {
            line = stmt.line;
            stmt.accept(this);
        }
    }
    private void compile(Expr expr) {
        if (expr != null) {
            line = expr.line;
            expr.accept(this);
        }
    }

    private static class Local {
        public final Token name;
        public final int scopeDepth;
        public final int index;
        public final boolean functionArgument;

        public Local(Token name, int scopeDepth, int index, boolean functionArgument) {
            this.name = name;
            this.scopeDepth = scopeDepth;
            this.index = index;
            this.functionArgument = functionArgument;
        }
    }
}
