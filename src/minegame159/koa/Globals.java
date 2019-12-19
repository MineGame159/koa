package minegame159.koa;

import minegame159.koa.ast.Parser;
import minegame159.koa.ast.Validator;

import java.util.HashMap;
import java.util.Map;

public class Globals {
    private final Map<String, Value> globals = new HashMap<>();

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

    public void run(String code) {
        Parser.Result parseResult = Parser.parse(code);
        parseResult.printErrors();
        if (parseResult.hadError()) return;

        Validator.Result validateResult = Validator.validate(parseResult.stmts);
        validateResult.printErrors();
        validateResult.printWarnings();
        if (validateResult.hadError()) return;

        try {
            Compiler.compile(parseResult.stmts, this).run();
        } catch (Error e) {
            System.out.println(e);
        }
    }
}
