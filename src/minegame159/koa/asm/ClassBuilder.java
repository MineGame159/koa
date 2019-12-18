package minegame159.koa.asm;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ClassBuilder {
    public final String name;
    public final String superName;

    private ClassWriter c = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

    public ClassBuilder(String name, String superName, String[] interfaces) {
        this.name = name;
        this.superName = superName;

        c.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, name, null, superName, interfaces);
    }

    public void field(String name, String descriptors) {
        c.visitField(Opcodes.ACC_PUBLIC, name, descriptors, null, null).visitEnd();
    }

    public MethodBuilder method(String name, String... descriptors) {
        String descriptor = ASM.methodDescriptor(descriptors);
        return new MethodBuilder(this, c.visitMethod(Opcodes.ACC_PUBLIC, name, descriptor, null, null), name, descriptor);
    }

    public void end() {
        c.visitEnd();
    }

    public byte[] build() {
        return c.toByteArray();
    }
}
