package minegame159.koa.tables;

import minegame159.koa.Value;

public class StringTable extends Value.Table {
    public String value;

    public StringTable(String value) {
        this.value = value;

        set("length", new Function() {
            @Override
            public int argCount() {
                return 0;
            }

            @Override
            public Value run(Value[] args) {
                return new Number(value.length());
            }
        });
    }

    @Override
    public String toString() {
        return value;
    }
}
