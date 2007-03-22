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
package org.vafer.dependency.asm;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.vafer.dependency.Console;

public final class RuntimeWrappingClassAdapter extends ClassAdapter implements Opcodes {
		
		private final String mapper;
		private final Console console;
		private String current;
	
		public RuntimeWrappingClassAdapter( final ClassVisitor cv, final String pMapperClassName ) {
			this(cv, pMapperClassName, null);
		}

		public RuntimeWrappingClassAdapter( final ClassVisitor cv, final String pMapper, final Console pConsole ) {
			super(cv);
			console = pConsole;
			mapper = pMapper;
		}
	
		public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
			super.visit(version, access, name, signature, superName, interfaces);
	
			current = name;
		}
	
		public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
	
			final MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
	
			return new MethodWrapper(mv);
		}
	
		private class MethodWrapper extends MethodAdapter {
	
			private final MethodVisitor mv;
	
			public MethodWrapper(final MethodVisitor pMv) {
				super(pMv);
				mv = pMv;
			}
				
			// static java.lang.Class       java.lang.Class.forName(java.lang.String)
			// static java.lang.Class       java.lang.Class.forName(java.lang.String, boolean, java.lang.ClassLoader)
			// static java.net.URL          java.lang.ClassLoader.getSystemResource(java.lang.String)
			// static java.io.InputStream   java.lang.ClassLoader.getSystemResourceAsStream(java.lang.String)
			// static java.util.Enumeration java.lang.ClassLoader.getSystemResources(java.lang.String)
			// java.lang.Class              java.lang.ClassLoader.loadClass(java.lang.String)
			// java.net.URL                 java.lang.ClassLoader.getResource(java.lang.String)
			// java.io.InputStream          java.lang.ClassLoader.getResourceAsStream(java.lang.String)
			
			private final Set methodsClass = new HashSet() {
				private static final long serialVersionUID = 1L;
				{
					add("forName");
				}
			};

			private final Set methodsClassLoader = new HashSet() {
				private static final long serialVersionUID = 1L;
				{
					add("loadClass");
					add("getSystemResource");
					add("getSystemResourceAsStream");
					add("getResource");
					add("getSystemResources");
					add("getResourceAsStream");
				}
			};
	
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				
				if (
						("java/lang/Class".equals(owner) && methodsClass.contains(name)) ||
				        ("java/lang/ClassLoader".equals(owner) && methodsClassLoader.contains(name))
				   ) {

					if (console != null) {
						console.println("wrapping " + owner + " in " + current);
					}
					
					mv.visitMethodInsn(INVOKESTATIC, mapper, "resolve", "(Ljava/lang/String;)Ljava/lang/String;");
					
				}

				mv.visitMethodInsn(opcode, owner, name, desc);
			}
		}
}
