package minegame159.koa;

import java.io.IOException;

public class KoaMain {
    public static void main(String[] args) throws IOException {
        Globals globals = new Globals();

        if (args.length == 1) {
            globals.run(args[0]);
        }
        else System.out.println("Usage: koa <script>");
    }
}
