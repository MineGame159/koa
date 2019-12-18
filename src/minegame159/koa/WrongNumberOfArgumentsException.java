package minegame159.koa;

public class WrongNumberOfArgumentsException extends Exception {
    public final int line;
    public final int expected, got;

    public WrongNumberOfArgumentsException(int line, int expected, int got) {
        super("At line " + line + " - " + "Expected: " + expected + ", Got: " + got + ".");
        this.line = line;
        this.expected = expected;
        this.got = got;
    }
}
