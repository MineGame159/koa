package minegame159.koa;

public class KoaClassLoader extends ClassLoader {
    public static final KoaClassLoader instance = new KoaClassLoader();

    Class define(String name, byte[] bytes) {
        return defineClass(name, bytes, 0, bytes.length);
    }
}
