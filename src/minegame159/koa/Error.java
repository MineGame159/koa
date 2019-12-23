package minegame159.koa;

public class Error extends RuntimeException {
    public String file;
    public int line;
    public String where;
    public String message;

    public Error(String file, int line, String where, String message) {
        this.file = file;
        this.line = line;
        this.where = where;
        this.message = message;
    }

    public Error(String file, int line, String message) {
        this.file = file;
        this.line = line;
        this.message = message;
    }

    @Override
    public String toString() {
        if (where != null) return "[" + file + " at line " + line + "] Error at " + where + ": " + message;
        else return "[" + file + " at line " + line + "] Error: " + message;
    }
}
