package minegame159.koa.tables;

import minegame159.koa.Value;

public class MathTable extends Value.Table {
    public static final MathTable instance = new MathTable();

    public MathTable() {
        set("PI", new Number(Math.PI));
        set("E", new Number(Math.E));

        set("round", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.round(args[0].toNumber()));
            }
        });
        set("floor", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.floor(args[0].toNumber()));
            }
        });
        set("ceil", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.ceil(args[0].toNumber()));
            }
        });

        set("random", new Function() {
            @Override
            public int argCount() {
                return 0;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.random());
            }
        });

        set("sin", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.sin(args[0].toNumber()));
            }
        });
        set("cos", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.cos(args[0].toNumber()));
            }
        });

        set("sqrt", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.sqrt(args[0].toNumber()));
            }
        });
        set("pow", new Function() {
            @Override
            public int argCount() {
                return 2;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.pow(args[0].toNumber(), args[1].toNumber()));
            }
        });
        set("atan", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.atan(args[0].toNumber()));
            }
        });

        set("toDegrees", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.toDegrees(args[0].toNumber()));
            }
        });
        set("toRadians", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.toRadians(args[0].toNumber()));
            }
        });

        set("abs", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.abs(args[0].toNumber()));
            }
        });
        set("exp", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.exp(args[0].toNumber()));
            }
        });
        set("log", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.log(args[0].toNumber()));
            }
        });

        set("min", new Function() {
            @Override
            public int argCount() {
                return 2;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.min(args[0].toNumber(), args[1].toNumber()));
            }
        });
        set("max", new Function() {
            @Override
            public int argCount() {
                return 2;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new Number(Math.max(args[0].toNumber(), args[1].toNumber()));
            }
        });
        set("clamp", new Function() {
            @Override
            public int argCount() {
                return 3;
            }

            @Override
            public Value run(Table table, Value... args) {
                double value = args[0].toNumber();

                if (value < args[1].toNumber()) return args[1];
                else if (value > args[2].toNumber()) return args[2];
                return args[0];
            }
        });
    }
}
