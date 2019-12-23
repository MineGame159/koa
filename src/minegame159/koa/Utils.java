package minegame159.koa;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean fileExists(String file) {
        return new File(file).isFile();
    }

    public static String readFile(String file) {
        try {
            return new String(Files.readAllBytes(Paths.get(file)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String resolvePath(String path) {
        List<String> pathComponents = new ArrayList<>();
        int start = 0;
        if (path.startsWith("./")) start = 2;
        for (int i = start; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                pathComponents.add(path.substring(start, i));
                start = i + 1;
            }
        }
        pathComponents.add(path.substring(start));

        List<String> finalComponents = new ArrayList<>(pathComponents.size());
        for (int i = 0; i < pathComponents.size(); i++) {
            String component = pathComponents.get(i);

            if (component.equals("..")){
                if (finalComponents.size() > 0) finalComponents.remove(finalComponents.size() - 1);
            }
            else finalComponents.add(component);
        }
        return String.join("/", finalComponents);
    }
}
