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
package org.vafer.dependency.resources;
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

		cw.visitSource("Mapper.java", null);

		{
			fv = cw.visitField(ACC_PRIVATE + ACC_FINAL + ACC_STATIC, "map", "Ljava/util/Map;", null, null);
			fv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitTypeInsn(NEW, "java/util/HashMap");
			mv.visitInsn(DUP);
			mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
			mv.visitFieldInsn(PUTSTATIC, pClassName, "map", "Ljava/util/Map;");
			Label l1 = new Label();
			mv.visitLabel(l1);

			for (Iterator it = pMapping.entrySet().iterator(); it.hasNext();) {
				final Map.Entry entry = (Map.Entry) it.next();
				final String oldResource = (String) entry.getKey();
				final String newResource = (String) entry.getValue();

				mv.visitFieldInsn(GETSTATIC, pClassName, "map", "Ljava/util/Map;");
				mv.visitLdcInsn(oldResource);
				mv.visitLdcInsn(newResource);
				mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
				mv.visitInsn(POP);				
			}
			
			mv.visitInsn(RETURN);
			mv.visitMaxs(3, 0);
			mv.visitEnd();
		}
		{
			mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
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
			mv = cw.visitMethod(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "resolve", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
			mv.visitCode();
			Label l0 = new Label();
			mv.visitLabel(l0);
			mv.visitFieldInsn(GETSTATIC, pClassName, "map", "Ljava/util/Map;");
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Map", "get", "(Ljava/lang/Object;)Ljava/lang/Object;");
			mv.visitTypeInsn(CHECKCAST, "java/lang/String");
			mv.visitVarInsn(ASTORE, 1);
			Label l1 = new Label();
			mv.visitLabel(l1);
			mv.visitVarInsn(ALOAD, 1);
			Label l2 = new Label();
			mv.visitJumpInsn(IFNONNULL, l2);
			Label l3 = new Label();
			mv.visitLabel(l3);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ARETURN);
			mv.visitLabel(l2);
			mv.visitVarInsn(ALOAD, 1);
			mv.visitInsn(ARETURN);
			Label l4 = new Label();
			mv.visitLabel(l4);
			mv.visitLocalVariable("pResourceName", "Ljava/lang/String;", null, l0, l4, 0);
			mv.visitLocalVariable("newResourceName", "Ljava/lang/String;", null, l1, l4, 1);
			mv.visitMaxs(2, 2);
			mv.visitEnd();
		}
		cw.visitEnd();

		return cw.toByteArray();
	}
}
