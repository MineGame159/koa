package minegame159.koa.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MethodBuilder {
    public final String name;
    public final String descriptor;
    public final ClassBuilder c;

    private MethodVisitor m;

    MethodBuilder(ClassBuilder c, MethodVisitor m, String name, String descriptor) {
        this.name = name;
        this.descriptor = descriptor;
        this.c = c;
        this.m = m;

        m.visitCode();
    }

    public void callSuper() {
        varInsn(Opcodes.ALOAD, 0);
        methodInsnSpecialD(c.superName, name, descriptor);
    }

    public void insn(int opcode) {
        m.visitInsn(opcode);
    }

    public void ldcInsn(Object value) {
        m.visitLdcInsn(value);
    }

    public void varInsn(int opcode, int var) {
        m.visitVarInsn(opcode, var);
    }

    public void fieldInsn(int opcode,String owner, String name, String descriptor) {
        m.visitFieldInsn(opcode, owner, name, descriptor);
    }

    public void methodInsnSpecial(String owner, String name, String... descriptors) {
        m.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, ASM.methodDescriptor(descriptors), false);
    }
    public void methodInsnSpecialD(String owner, String name, String descriptor) {
        m.visitMethodInsn(Opcodes.INVOKESPECIAL, owner, name, descriptor, false);
    }
    public void methodInsn(String owner, String name, String... descriptors) {
        m.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, ASM.methodDescriptor(descriptors), false);
    }
    public void methodStaticInsn(String owner, String name, String... descriptors) {
        m.visitMethodInsn(Opcodes.INVOKESTATIC, owner, name, ASM.methodDescriptor(descriptors), false);
    }

    public void jumpInsn(int opcode, Label label) {
        m.visitJumpInsn(opcode, label);
    }

    public void label(Label label) {
        m.visitLabel(label);
    }

    public void typeInsn(int opcode, String type) {
        m.visitTypeInsn(opcode, type);
    }

    public void end() {
        m.visitMaxs(0, 0);
        m.visitEnd();
    }
}
