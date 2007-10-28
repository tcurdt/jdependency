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
import org.vafer.dependency.asm.Remapper;
import org.vafer.dependency.resources.ResolverUtils;

final class RuntimeResourceResolvingClassAdapter extends ClassAdapter implements Opcodes {
		
		private final String runimeClass = "org/vafer/dependency/resources/runtime/Mapper";
		private final Remapper mapper;
		private String current;
	
		public RuntimeResourceResolvingClassAdapter( final ClassVisitor cv, final Remapper pMapper ) {
			super(cv);
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
				

			private void inject( MethodVisitor mv, String method ) {

				// "old"				
				mv.visitTypeInsn(NEW, "java/lang/StringBuffer");
				// "old", stringbuffer
				mv.visitInsn(DUP);
				// "old", stringbuffer, stringbuffer
				mv.visitLdcInsn(mapper.map(""));
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
				mv.visitMethodInsn(INVOKESTATIC, runimeClass, method, "(Ljava/lang/String;)Ljava/lang/String;");
				// "old", "prefixnew"
				mv.visitInsn(SWAP);
				// "prefixnew", "old"
				mv.visitInsn(POP);
				// "prefixnew"
			}
			
			public void visitMethodInsn(int opcode, String owner, String name, String desc) {
				
				if (ResolverUtils.needsResourceResolving(owner, name)) {

					System.out.println("Wrapping resource access " + name + " in " + current);
					
					inject(mv, "resolveResource");
										
				} else if (ResolverUtils.needsClassResolving(owner, name)) {

					System.out.println("Wrapping class access " + name + " in " + current);

					inject(mv, "resolveClass");
					
				}

				mv.visitMethodInsn(opcode, owner, name, desc);
			}
		}
}