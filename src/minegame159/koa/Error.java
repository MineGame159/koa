package minegame159.koa;

public class Error extends RuntimeException {
    public int line;
    public String where;
    public String message;

    public Error(int line, String where, String message) {
        this.line = line;
        this.where = where;
        this.message = message;
    }

    public Error(int line, String message) {
        this.line = line;
        this.message = message;
    }

    @Override
    public String toString() {
        if (where != null) return "[line " + line + "] Error at " + where + ": " + message;
        else return "[line " + line + "] Error: " + message;
    }
}
