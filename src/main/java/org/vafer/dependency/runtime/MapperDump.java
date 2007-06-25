/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vafer.dependency.runtime;
import java.util.Iterator;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class MapperDump implements Opcodes {

	public static byte[] dump( final String pClassName, final Map pMapping ) throws Exception {

		ClassWriter cw = new ClassWriter(0);
		FieldVisitor fv;
		MethodVisitor mv;

		cw.visit(V1_2, ACC_PUBLIC + ACC_FINAL + ACC_SUPER, pClassName, null, "java/lang/Object", null);

		cw.visitSource("Mapper.java", null);

		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "debug", "Z", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "clazzName", "Ljava/lang/String;", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "mapping", "Ljava/util/HashMap;", null, null);
			fv.visitEnd();
		}
		{
			fv = cw.visitField(ACC_STATIC + ACC_SYNTHETIC, "class$0", "Ljava/lang/Class;", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			Label l1 = new Label();
			Label l2 = new Label();
			mv.visitTryCatchBlock(l0, l1, l2, "java/lang/ClassNotFoundException");
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(7, l3);
			mv.visitLdcInsn("true");
			mv.visitLdcInsn("org.vafer.dependency.relocation.debug");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equalsIgnoreCase", "(Ljava/lang/String;)Z");
			mv.visitFieldInsn(PUTSTATIC, pClassName, "debug", "Z");
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLineNumber(8, l4);
			mv.visitFieldInsn(GETSTATIC, pClassName, "class$0", "Ljava/lang/Class;");
			mv.visitInsn(DUP);
			Label l5 = new Label();
			mv.visitJumpInsn(IFNONNULL, l5);
			mv.visitInsn(POP);
			mv.visitLabel(l0);
			mv.visitLdcInsn(pClassName.replace('/', '.'));
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Class", "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
			mv.visitLabel(l1);
			mv.visitInsn(DUP);
			mv.visitFieldInsn(PUTSTATIC, pClassName, "class$0", "Ljava/lang/Class;");
			mv.visitJumpInsn(GOTO, l5);
			mv.visitLabel(l2);
			mv.visitTypeInsn(NEW, "java/lang/NoClassDefFoundError");
			mv.visitInsn(DUP_X1);
			mv.visitInsn(SWAP);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Throwable", "getMessage", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/NoClassDefFoundError", "<init>", "(Ljava/lang/String;)V");
			mv.visitInsn(ATHROW);
			mv.visitLabel(l5);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;");
			mv.visitFieldInsn(PUTSTATIC, pClassName, "clazzName", "Ljava/lang/String;");
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitLineNumber(13, l6);
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, pClassName, "mapping", "Ljava/util/HashMap;");
			
			for (Iterator it = pMapping.keySet().iterator(); it.hasNext();) {
				final String oldName = (String) it.next();
				final String newName = (String) pMapping.get(oldName);

				mv.visitFieldInsn(GETSTATIC, pClassName, "mapping", "Ljava/util/HashMap;");
				mv.visitLdcInsn(oldName);
				mv.visitLdcInsn(newName);
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
				mv.visitInsn(POP);
			}
			
			Label l10 = new Label();
			mv.visitLabel(l10);
			mv.visitLineNumber(5, l10);
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(5, l0);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
			mv.visitInsn(RETURN);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLocalVariable("this", "L" + pClassName + ";", null, l0, l1, 0);
			mv.visitMaxs(1, 1);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "resolveResource", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(18, l0);
			mv.visitFieldInsn(GETSTATIC, pClassName, "mapping", "Ljava/util/HashMap;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/HashMap", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
			mv.visitTypeInsn(CHECKCAST, "java/lang/String");
			mv.visitVarInsn(ASTORE, 1);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(20, l1);
			mv.visitVarInsn(ALOAD, 1);
			Label l2 = new Label();
			mv.visitJumpInsn(IFNONNULL, l2);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(21, l3);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitVarInsn(ASTORE, 1);
			mv.visitLabel(l2);
			mv.visitLineNumber(24, l2);
			mv.visitFieldInsn(GETSTATIC, pClassName, "debug", "Z");
			Label l4 = new Label();
			mv.visitJumpInsn(IFEQ, l4);
			Label l5 = new Label();
			mv.visitLabel(l5);
			mv.visitLineNumber(25, l5);
			mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
			mv.visitInsn(DUP);
			mv.visitLdcInsn("*** ");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V");
			mv.visitFieldInsn(GETSTATIC, pClassName, "clazzName", "Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
			mv.visitLdcInsn(" mapping resource request ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
			mv.visitLdcInsn(" -> ");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
			mv.visitLabel(l4);
			mv.visitLineNumber(28, l4);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ARETURN);
			Label l6 = new Label();
			mv.visitLabel(l6);
			mv.visitLocalVariable("oldName", "Ljava/lang/String;", null, l0, l6, 0);
			mv.visitLocalVariable("newName", "Ljava/lang/String;", null, l1, l6, 1);
			mv.visitMaxs(4, 2);
			mv.visitEnd();
			}
		{
			mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "resolveClass", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitLineNumber(26, l0);
			mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitIntInsn(BIPUSH, 46);
			mv.visitIntInsn(BIPUSH, 47);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace", "(CC)Ljava/lang/String;");
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
			mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V");
			mv.visitLdcInsn(".class");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");
			mv.visitVarInsn(ASTORE, 1);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitLineNumber(27, l1);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKESTATIC, pClassName, "resolveResource", "(Ljava/lang/String;)Ljava/lang/String;");
			mv.visitVarInsn(ASTORE, 2);
			Label l2 = new Label();
			mv.visitLabel(l2);
			mv.visitLineNumber(28, l2);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitInsn(ICONST_0);
			mv.visitVarInsn(ALOAD, 2);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I");
			mv.visitIntInsn(BIPUSH, 6);
			mv.visitInsn(ISUB);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "substring", "(II)Ljava/lang/String;");
			mv.visitIntInsn(BIPUSH, 47);
			mv.visitIntInsn(BIPUSH, 46);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "replace", "(CC)Ljava/lang/String;");
			mv.visitVarInsn(ASTORE, 3);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitLineNumber(29, l3);
			mv.visitVarInsn(ALOAD, 3);
			mv.visitInsn(ARETURN);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLocalVariable("oldName", "Ljava/lang/String;", null, l0, l4, 0);
			mv.visitLocalVariable("oldResourceName", "Ljava/lang/String;", null, l1, l4, 1);
			mv.visitLocalVariable("newResourceName", "Ljava/lang/String;", null, l2, l4, 2);
			mv.visitLocalVariable("newName", "Ljava/lang/String;", null, l3, l4, 3);
			mv.visitMaxs(5, 4);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
