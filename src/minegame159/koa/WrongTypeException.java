package minegame159.koa;

public class WrongTypeException extends Exception {
    public int line;

    public WrongTypeException(int line) {
        super("At line " + line);
        this.line = line;
    }
}
