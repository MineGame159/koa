package minegame159.koa;

public class Warning {
    public String file;
    public int line;
    public String message;

    public Warning(String file, int line, String message) {
        this.file = file;
        this.line = line;
        this.message = message;
    }

    @Override
    public String toString() {
        return "[" + file + " at line " + line + "] Warning: " + message;
    }
}
