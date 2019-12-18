package minegame159.koa;

public class WrongTypeException extends Exception {
    public int line;
    public Value.Type expected, got;

    public WrongTypeException(int line, Value.Type expected, Value.Type got) {
        super("At line " + line + " Expected: " + expected + " Got: " + got);
        this.line = line;
        this.expected = expected;
        this.got = got;
    }
}
