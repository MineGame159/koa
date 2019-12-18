package minegame159.koa;

public class Warning {
    public int line;
    public String message;

    public Warning(int line, String message) {
        this.line = line;
        this.message = message;
    }

    @Override
    public String toString() {
        return "[line " + line + "] Warning: " + message;
    }
}
