package minegame159.koa.tables;

import minegame159.koa.Value;

public class StringTable extends Value.Table {
    public String value;

    public StringTable(String value) {
        this.value = value;

        Table mt = new Table();
        mt.set("__add", new Function() {
            @Override
            public int argCount() {
                return 1;
            }

            @Override
            public Value run(Table table, Value... args) {
                return new StringTable(value + args[0]);
            }
        });
        setMetatable(mt);

        set("length", new Function() {
            @Override
            public int argCount() {
                return 0;
            }

            @Override
            public Value run(Table table, Value[] args) {
                return new Number(value.length());
            }
        });
    }

    @Override
    public String toString() {
        return value;
    }
}
