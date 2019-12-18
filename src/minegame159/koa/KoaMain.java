package minegame159.koa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class KoaMain {
    public static void main(String[] args) throws IOException {
        Globals globals = new Globals();

        if (args.length == 1) {
            globals.run(new String(Files.readAllBytes(Paths.get(args[0]))));
        }
        else System.out.println("Usage: koa <script>");
    }
}
