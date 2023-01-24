package org.urts.junit5Extension;

import org.urts.Ekstazi;
import org.urts.agent.Instr;
import org.urts.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import static org.urts.junit5Extension.Junit5Helper.isTestClassTransformNeeded;

public class JUnit5ForkCFT implements ClassFileTransformer {

    public static class Junit5ForkClassVisitor extends ClassVisitor {
        public Junit5ForkClassVisitor (ClassVisitor cv) {
            super(Instr.ASM_API_VERSION, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return new Junit5ForkMethodVisitor(super.visitMethod(access, name, desc, signature, exceptions));
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return null;
        }

        private static class Junit5ForkMethodVisitor extends MethodVisitor {
            public Junit5ForkMethodVisitor (MethodVisitor mv) {
                super(Instr.ASM_API_VERSION, mv);
            }

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return null;
            }
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (isTestClassTransformNeeded(className) && !Ekstazi.inst().isClassAffected(className.replace("/", "."))) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            Junit5ForkClassVisitor visitor = new Junit5ForkClassVisitor(classWriter);
            classReader.accept(visitor, 0);
            return classWriter.toByteArray();
        }
        return null;
    }
}
