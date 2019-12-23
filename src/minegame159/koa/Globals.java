package minegame159.koa;

import minegame159.koa.ast.Parser;
import minegame159.koa.ast.Validator;
import minegame159.koa.tables.MathTable;

import java.util.HashMap;
import java.util.Map;

public class Globals {
    private Map<String, Value> globals = new HashMap<>();
    private String file;
    private boolean printedError;
    private Map<String, Runnable> modules = new HashMap<>();

    public Globals() {
        set("print", new Value.Function() {
            @Override
            public int argCount() {
                return -1;
            }

            @Override
            public Value run(Table table, Value[] args) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < args.length; i++) sb.append(args[i]);
                System.out.println(sb);
                return Value.NULL;
            }
        });

        set("require", new Value.Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                Globals globals = new Globals();
                globals.modules = modules;
                globals.run(file + "/../" + args[0], printedError);
                printedError = globals.printedError;
                if (globals.contains("export")) return globals.get("export");
                else return Value.NULL;
            }
        });

        set("setMetatable", new Value.Function() {
            @Override
            public int argCount() {
                return 2;
            }

            @Override
            public Value run(Table table, Value... args) {
                args[0].toTable().setMetatable(args[1].toTable());
                return Value.NULL;
            }
        });
        set("getMetatable", new Value.Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                Table metatable = args[0].toTable().getMetatable();
                return metatable != null ? metatable : Value.NULL;
            }
        });

        set("time", new Value.Function() {
            @Override
            public int argCount() {
                return 0;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(System.currentTimeMillis() / 1000.0);
            }
        });

        set("Math", MathTable.instance);
    }

    public void set(String key, Value value) {
        globals.put(key, value);
    }
    public Value get(String key) {
        return globals.get(key);
    }
    public Value getOrNull(String key) {
        Value value = globals.get(key);
        return value != null ? value : Value.NULL;
    }

    public boolean contains(String key) {
        return globals.containsKey(key);
    }

    public void run(String file) {
        run(file, false);
    }
    private void run(String file, boolean printedError) {
        if (!Utils.fileExists(file)) return;
        this.file = Utils.resolvePath(file);
        this.printedError = printedError;

        Runnable runnable;
        if (modules.containsKey(this.file)) {
            runnable = modules.get(this.file);
            try {
                Object gl = runnable.getClass().getField("globals").get(runnable);
                globals = (Map<String, Value>) gl.getClass().getDeclaredField("globals").get(gl);
                globals.clear();
            } catch (IllegalAccessException | NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        else {
            Parser.Result parseResult = Parser.parse(this.file);
            parseResult.printErrors();
            if (parseResult.hadError()) return;

            Validator.Result validateResult = Validator.validate(this.file, parseResult.stmts);
            validateResult.printErrors();
            validateResult.printWarnings();
            if (validateResult.hadError()) return;

            runnable = Compiler.compile(this.file, parseResult.stmts, this);
            modules.put(this.file, runnable);
        }

        try {
            runnable.run();
        } catch (Error e) {
            if (!this.printedError) {
                System.out.println(e);
                this.printedError = true;
            }
        }
    }
}
