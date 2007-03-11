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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;

public abstract class DependencyVisitor extends DelegatingVisitor implements ClassVisitor, FieldVisitor, MethodVisitor {

	abstract protected String visitDependency( final String pClassName );

	
	public DependencyVisitor() {
		super(new EmptyVisitor());
	}

	public DependencyVisitor(ClassVisitor pClassVisitor) {
		super(pClassVisitor);
	}


	
	private String[] translateNames( final String[] names ) {
		if (names == null) {
			return null;
		}
		
		for (int i = 0; i < names.length; i++) {
			names[i] = translateName(names[i]);
		}
		
		return names;
	}
	
	private String translateName( final String name ) {
		if (name == null) {
			return null;
		}
		
		return visitDependency(name);
	}
	
	private String translateDescription( final String descr ) {
		if (descr == null) {
			return null;
		}
		
		final char[] s = descr.toCharArray();
		
		final StringBuffer result = new StringBuffer();
		final StringBuffer name = new StringBuffer();
		
		boolean record = false;
		
		for (int i = 0; i < s.length; i++) {
			final char c = s[i];
			if (record) {
				if (c == ';' || c =='<') {
					record = false;
					result.append(translateName(name.toString()));
					result.append(c);
				} else {
					name.append(c);
				}
			} else {
				if (c == 'L') {
					record = true;
					name.setLength(0);
				}
				result.append(c);
			}
		}
		
		return result.toString();
	}

	
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {

		name = translateName(name);
		signature = translateDescription(signature);
		superName = translateName(superName);
		interfaces = translateNames(interfaces);

		super.visit(version, access, name, signature, superName, interfaces);
	}

	
	
	public AnnotationVisitor doVisitFieldAnnotation(String desc, boolean visible) {

		desc = translateDescription(desc);
		
		return super.doVisitFieldAnnotation(desc, visible);
	}
	
	public AnnotationVisitor doVisitMethodAnnotation(String desc, boolean visible) {

		desc = translateDescription(desc);
		
		return super.doVisitMethodAnnotation(desc, visible);
	}

	public FieldVisitor doVisitField(int access, String name, String desc, String signature, Object value) {

		desc = translateDescription(desc);
		signature = translateDescription(signature);

		return super.doVisitField(access, name, desc, signature, value);
	}

	public MethodVisitor doVisitMethod(int access, String name, String desc, String signature, String[] exceptions) {

		desc = translateDescription(desc);
		signature = translateDescription(signature);
		exceptions = translateNames(exceptions);

		return super.doVisitMethod(access, name, desc, signature, exceptions);
	}

	// MethodVisitor

	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {

		desc = translateName(desc);

		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	public void visitTypeInsn(int opcode, String desc) {

		desc = translateName(desc);
		
		super.visitTypeInsn(opcode, desc);
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {

		owner = translateName(owner);
		desc = translateDescription(desc);
		
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {

		owner = translateName(owner);
		desc = translateDescription(desc);
		
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	public void visitLdcInsn(Object cst) {
		if (cst instanceof Type) {
			final Type type = (Type) cst;
			cst = Type.getType(translateDescription(type.getDescriptor()));
		}
		super.visitLdcInsn(cst);
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {

		desc = translateDescription(desc);
		
		super.visitMultiANewArrayInsn(desc, dims);
	}

	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {

		desc = translateDescription(desc);		
		signature = translateDescription(signature);
		
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}


	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		
		type = translateName(type);
		
		super.visitTryCatchBlock(start, end, handler, type);
	}

}
