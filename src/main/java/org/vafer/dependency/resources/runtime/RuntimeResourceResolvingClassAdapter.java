/**
 * 
 */
package org.vafer.dependency.resources.runtime;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

final class RuntimeResourceResolvingClassAdapter extends ClassAdapter implements Opcodes {
		
		private final String mapper;
		private String current;
	
		public RuntimeResourceResolvingClassAdapter( final ClassVisitor cv, final String pMapperClassName ) {
			super(cv);
			mapper = pMapperClassName;
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
			
			private final Set resolveClassMethodsClass = new HashSet() {
				private static final long serialVersionUID = 1L;
				{
					add("forName");
				}
			};
			private final Set resolveResouceMethodsClass = new HashSet() {
				private static final long serialVersionUID = 1L;
				{
				}
			};

			private final Set resolveClassMethodsClassLoader = new HashSet() {
				private static final long serialVersionUID = 1L;
				{
					add("loadClass");
				}
			};
			private final Set resolveResourceMethodsClassLoader = new HashSet() {
				private static final long serialVersionUID = 1L;
				{
					add("getSystemResource");
					add("getSystemResourceAsStream");
					add("getResource");
					add("getSystemResources");
					add("getResourceAsStream");
				}
			};

			private void inject( MethodVisitor mv, String method ) {

				// "old"				
				mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
				// "old", stringbuffer
				mv.visitInsn(DUP);
				// "old", stringbuffer, stringbuffer
				mv.visitLdcInsn("prefix");
				// "old", stringbuffer, stringbuffer, "prefix"
				mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuffer", "<init>", "(Ljava/lang/String;)V");
				// "old", stringbuffer
				mv.visitInsn(DUP2);
				// "old", stringbuffer, "old", stringbuffer
                mv.visitInsn(POP);
				// "old", stringbuffer, "old"
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "append", "(Ljava/lang/String;)Ljava/lang/StringBuffer;");
				// "old", stringbuffer
				mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuffer", "toString", "()Ljava/lang/String;");				
				// "old", "prefixold"
				mv.visitMethodInsn(INVOKESTATIC, mapper, method, "(Ljava/lang/String;)Ljava/lang/String;");
				// "old", "prefixnew"
				mv.visitInsn(SWAP);
				// "prefixnew", "old"
				mv.visitInsn(POP);
				// "prefixnew"
			}
			
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				
				if (("java/lang/Class".equals(owner) && resolveResouceMethodsClass.contains(name)) ||
				    ("java/lang/ClassLoader".equals(owner) && resolveResourceMethodsClassLoader.contains(name))) {

					System.out.println("Wrapping resource access " + name + " in " + current);
					
					inject(mv, "resolveResource");
										
				} else

				if (("java/lang/Class".equals(owner) && resolveClassMethodsClass.contains(name)) ||
					("java/lang/ClassLoader".equals(owner) && resolveClassMethodsClassLoader.contains(name))) {

					System.out.println("Wrapping class access " + name + " in " + current);

					inject(mv, "resolveClass");
					
				}

				mv.visitMethodInsn(opcode, owner, name, desc);
			}
		}
}