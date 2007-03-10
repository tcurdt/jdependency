package org.vafer.dependency;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MapperDump implements Opcodes {

	public static byte[] dump( final String pClassName, final Map pMapping ) throws Exception {

		ClassWriter cw = new ClassWriter(false);
		FieldVisitor fv;
		MethodVisitor mv;

		cw.visit(V1_2, ACC_PUBLIC + ACC_SUPER, pClassName, null, "java/lang/Object", null);

		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "map", "Ljava/util/Map;", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, pClassName, "map", "Ljava/util/Map;");
			
			for (Iterator it = pMapping.entrySet().iterator(); it.hasNext();) {
				final Map.Entry entry = (Map.Entry) it.next();
				final String oldResource = (String) entry.getKey();
				final String newResource = (String) entry.getKey();

				mv.visitFieldInsn(GETSTATIC, pClassName, "map", "Ljava/util/Map;");
				mv.visitLdcInsn(oldResource);
				mv.visitLdcInsn(newResource);
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
				mv.visitInsn(POP);				
			}
			
			mv.visitInsn(RETURN);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitInsn(RETURN);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "resolve", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
			mv.visitCode();
			mv.visitFieldInsn(GETSTATIC, pClassName, "map", "Ljava/util/Map;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
			mv.visitTypeInsn(CHECKCAST, "java/lang/String");
			mv.visitVarInsn(ASTORE, 1);
			mv.visitVarInsn(ALOAD, 1);
			Label l2 = new Label();
			mv.visitJumpInsn(IFNONNULL, l2);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ARETURN);
			mv.visitLabel(l2);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ARETURN);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
