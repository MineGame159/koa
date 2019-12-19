package minegame159.koa;

import java.util.HashMap;
import java.util.Map;

public abstract class Value {
    public enum Type {
        Null,
        Bool,
        Number,
        Table,
        Function
    }

    public static Null NULL = new Null();

    public static class Null extends Value {
        public Null() {
            super(Type.Null);
        }

        @Override
        public String toString() {
            return "null";
        }
    }

    public static class Bool extends Value {
        public boolean value;

        public Bool(boolean value) {
            super(Type.Bool);
            this.value = value;
        }

        @Override
        public String toString() {
            return value ? "true" : "false";
        }
    }

    public static class Number extends Value {
        public double value;

        public Number(double value) {
            super(Type.Number);
            this.value = value;
        }

        @Override
        public String toString() {
            return Double.toString(value);
        }
    }

    public static class Table extends Value {
        private Map<String, Value> values = new HashMap<>(1);
        private Table metatable;

        public Table() {
            super(Type.Table);
        }

        public void set(String key, Value value) {
            if (value == null) values.remove(key);
            else values.put(key, value);
        }
        public Value get(String key) {
            Value value = values.get(key);
            if (metatable != null && value == null) value = metatable.get(key);
            return value;
        }
        public Value getOrNull(String key) {
            Value value = values.get(key);
            if (metatable != null && value == null) value = metatable.get(key);
            return value != null ? value : NULL;
        }

        public void setMetatable(Table metatable) {
            this.metatable = metatable;
        }
        public Table getMetatable() {
            return metatable;
        }

        @Override
        public String toString() {
            return "table";
        }
    }

    public abstract static class Function extends Value {
        public Function() {
            super(Type.Function);
        }

        public abstract int argCount();

        public abstract Value run(Table table, Value... args);

        @Override
        public String toString() {
            return "function";
        }
    }

    public final Type type;

    public Value(Type type) {
        this.type = type;
    }

    public boolean isNull() {
        return type == Type.Null;
    }
    public boolean isBool() {
        return type == Type.Bool;
    }
    public boolean isNumber() {
        return type == Type.Number;
    }
    public boolean isTable() {
        return type == Type.Table;
    }
    public boolean isFunction() {
        return type == Type.Function;
    }

    public boolean toBool() {
        return ((Bool) this).value;
    }
    public double toNumber() {
        return ((Number) this).value;
    }
    public Table toTable() {
        return (Table) this;
    }
    public Function toFunction() {
        return (Function) this;
    }

    public boolean isTruthy() {
        switch (type) {
            default:
            case Null:   return false;
            case Function:
            case Table:
            case Number: return true;
            case Bool:   return ((Bool) this).value;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Value) || type != ((Value) obj).type) return false;

        switch (type) {
            case Null:   return true;
            case Bool:   return toBool() == ((Bool) obj).value;
            case Number: return toNumber() == ((Number) obj).value;
            case Function:
            case Table:  return this == obj;
            default:     return false;
        }
    }
}
