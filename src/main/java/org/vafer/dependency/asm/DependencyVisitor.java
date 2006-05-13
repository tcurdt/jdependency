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
import org.objectweb.asm.commons.EmptyVisitor;

public abstract class DependencyVisitor extends DelegatingVisitor implements ClassVisitor, FieldVisitor, MethodVisitor {

	abstract protected String visitDependency( final String pClassName );

	
	public DependencyVisitor() {
		super(new EmptyVisitor());
	}

	public DependencyVisitor(ClassVisitor pClassVisitor) {
		super(pClassVisitor);
	}



//	private final static List asList(Object[] array) {
//		if (array == null) {
//			return null;
//		}
//		return Arrays.asList(array);
//	}

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
		
		return visitDependency(name.replace('/', '.')).replace('.', '/');
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
//		StringBuffer sb = new StringBuffer();
//		sb.append("visit").append(": ");
//		sb.append("version").append("=").append(version).append(", ");
//		sb.append("access").append("=").append(access).append(", ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("signature").append("=").append(signature).append(", ");
//		sb.append("superName").append("=").append(superName).append(", ");
//		sb.append("interfaces").append("=").append(asList(interfaces)).append(", ");
//		System.out.println(sb.toString());

		name = translateName(name);
		signature = translateDescription(signature);
		superName = translateName(superName);
		interfaces = translateNames(interfaces);

		super.visit(version, access, name, signature, superName, interfaces);
	}

//	public AnnotationVisitor visitClassAnnotation(String desc, boolean visible) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitClassAnnotation").append(": ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("visible").append("=").append(visible).append(", ");
//		System.out.println(sb.toString());
//
//		desc = translateDescription(desc);
//
//		return super.visitClassAnnotation(desc, visible);
//	}
	
	public AnnotationVisitor doVisitFieldAnnotation(String desc, boolean visible) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitClassAnnotation").append(": ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("visible").append("=").append(visible).append(", ");
//		System.out.println(sb.toString());

		desc = translateDescription(desc);
		
		return super.doVisitFieldAnnotation(desc, visible);
	}
	
	public AnnotationVisitor doVisitMethodAnnotation(String desc, boolean visible) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitClassAnnotation").append(": ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("visible").append("=").append(visible).append(", ");
//		System.out.println(sb.toString());

		desc = translateDescription(desc);
		
		return super.doVisitMethodAnnotation(desc, visible);
	}

//	public void visitAttribute(Attribute attr) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitField").append(": ");
//		sb.append("attr").append("=").append(attr).append(", ");
//		System.out.println(sb.toString());
//	}

	public FieldVisitor doVisitField(int access, String name, String desc, String signature, Object value) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitField").append(": ");
//		sb.append("access").append("=").append(access).append(", ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("signature").append("=").append(signature).append(", ");
//		sb.append("value").append("=").append(value).append(", ");
//		System.out.println(sb.toString());

		desc = translateDescription(desc);
		signature = translateDescription(signature);

		return super.doVisitField(access, name, desc, signature, value);
	}

	public MethodVisitor doVisitMethod(int access, String name, String desc, String signature, String[] exceptions) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitMethod").append(": ");
//		sb.append("access").append("=").append(access).append(", ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("signature").append("=").append(signature).append(", ");
//		sb.append("exceptions").append("=").append(asList(exceptions)).append(", ");
//		System.out.println(sb.toString());

		desc = translateDescription(desc);
		signature = translateDescription(signature);
		exceptions = translateNames(exceptions);

		return super.doVisitMethod(access, name, desc, signature, exceptions);
	}

//	public void visitSource(String source, String debug) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitSource").append(": ");
//		sb.append("source").append("=").append(source).append(", ");
//		sb.append("debug").append("=").append(debug).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitInnerClass(String name, String outerName, String innerName, int access) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitInnerClass").append(": ");
//		sb.append("name").append("=").append(access).append(", ");
//		sb.append("outerName").append("=").append(outerName).append(", ");
//		sb.append("innerName").append("=").append(innerName).append(", ");
//		sb.append("access").append("=").append(access).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitOuterClass(String owner, String name, String desc) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitOuterClass").append(": ");
//		sb.append("owner").append("=").append(owner).append(", ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		System.out.println(sb.toString());
//	}

	// MethodVisitor

	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitParameterAnnotation").append(": ");
//		sb.append("parameter").append("=").append(parameter).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("visible").append("=").append(visible).append(", ");
//		System.out.println(sb.toString());

		desc = translateName(desc);

		return super.visitParameterAnnotation(parameter, desc, visible);
	}

	public void visitTypeInsn(int opcode, String desc) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitTypeInsn").append(": ");
//		sb.append("opcode").append("=").append(opcode).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		System.out.println(sb.toString());

		desc = translateName(desc);
		
		super.visitTypeInsn(opcode, desc);
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitFieldInsn").append(": ");
//		sb.append("opcode").append("=").append(opcode).append(", ");
//		sb.append("owner").append("=").append(owner).append(", ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		System.out.println(sb.toString());

		owner = translateName(owner);
		desc = translateDescription(desc);
		
		super.visitFieldInsn(opcode, owner, name, desc);
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitMethodInsn").append(": ");
//		sb.append("opcode").append("=").append(opcode).append(", ");
//		sb.append("owner").append("=").append(owner).append(", ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		System.out.println(sb.toString());

		owner = translateName(owner);
		desc = translateDescription(desc);
		
		super.visitMethodInsn(opcode, owner, name, desc);
	}

	public void visitLdcInsn(Object cst) {
		StringBuffer sb = new StringBuffer();
		sb.append("visitLdcInsn").append(": ");
		sb.append("cst").append("=").append(cst).append(", ");
		System.out.println(sb.toString());

		//FIXME
		
		super.visitLdcInsn(cst);
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitMultiANewArrayInsn").append(": ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("dims").append("=").append(dims).append(", ");
//		System.out.println(sb.toString());

		desc = translateDescription(desc);
		
		super.visitMultiANewArrayInsn(desc, dims);
	}

	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitLocalVariable").append(": ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("signature").append("=").append(signature).append(", ");
//		sb.append("start").append("=").append(start).append(", ");
//		sb.append("end").append("=").append(end).append(", ");
//		sb.append("index").append("=").append(index).append(", ");
//		System.out.println(sb.toString());

		desc = translateDescription(desc);
		signature = translateDescription(signature);
		
		super.visitLocalVariable(name, desc, signature, start, end, index);
	}

//	public AnnotationVisitor visitAnnotationDefault() {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitAnnotationDefault").append(": ");
//		System.out.println(sb.toString());		
//		return this;
//	}

//	public void visitCode() {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitCode").append(": ");
//		System.out.println(sb.toString());
//	}

//	public void visitInsn(int opcode) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitInsn").append(": ");
//		sb.append("opcode").append("=").append(opcode).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitIntInsn(int opcode, int operand) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitIntInsn").append(": ");
//		sb.append("opcode").append("=").append(opcode).append(", ");
//		sb.append("operand").append("=").append(operand).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitVarInsn(int opcode, int var) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitVarInsn").append(": ");
//		sb.append("opcode").append("=").append(opcode).append(", ");
//		sb.append("var").append("=").append(var).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitJumpInsn(int opcode, Label label) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitJumpInsn").append(": ");
//		sb.append("opcode").append("=").append(opcode).append(", ");
//		sb.append("label").append("=").append(label).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitLabel(Label label) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitLabel").append(": ");
//		sb.append("label").append("=").append(label).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitIincInsn(int var, int increment) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitIincInsn").append(": ");
//		sb.append("var").append("=").append(increment).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitTableSwitchInsn").append(": ");
//		sb.append("min").append("=").append(min).append(", ");
//		sb.append("max").append("=").append(max).append(", ");
//		sb.append("dflt").append("=").append(dflt).append(", ");
//		sb.append("labels").append("=").append(asList(labels)).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitLookupSwitchInsn").append(": ");
//		sb.append("dflt").append("=").append(dflt).append(", ");
//		sb.append("keys").append("=").append(keys).append(", ");
//		sb.append("labels").append("=").append(asList(labels)).append(", ");
//		System.out.println(sb.toString());
//	}

	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitTryCatchBlock").append(": ");
//		sb.append("start").append("=").append(start).append(", ");
//		sb.append("end").append("=").append(end).append(", ");
//		sb.append("handler").append("=").append(handler).append(", ");
//		sb.append("type").append("=").append(type).append(", ");
//		System.out.println(sb.toString());
		
		type = translateName(type);
		
		super.visitTryCatchBlock(start, end, handler, type);
	}

//	public void visitLineNumber(int line, Label start) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitLineNumber").append(": ");
//		sb.append("line").append("=").append(line).append(", ");
//		sb.append("start").append("=").append(start).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitMaxs(int maxStack, int maxLocals) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitMaxs").append(": ");
//		sb.append("maxStack").append("=").append(maxStack).append(", ");
//		sb.append("maxLocals").append("=").append(maxLocals).append(", ");
//		System.out.println(sb.toString());
//	}

	// AnnotationVisitor
	
//	public void visit(String name, Object value) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visit").append(": ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("value").append("=").append(value).append(", ");
//		System.out.println(sb.toString());
//	}

//	public void visitEnum(String name, String desc, String value) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitEnum").append(": ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		sb.append("value").append("=").append(value).append(", ");
//		System.out.println(sb.toString());
//	}

//	public AnnotationVisitor visitAnnotation(String name, String desc) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitAnnotation").append(": ");
//		sb.append("name").append("=").append(name).append(", ");
//		sb.append("desc").append("=").append(desc).append(", ");
//		System.out.println(sb.toString());
//		return this;
//	}

//	public AnnotationVisitor visitArray(String name) {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitArray").append(": ");
//		sb.append("name").append("=").append(name).append(", ");
//		System.out.println(sb.toString());
//		return this;
//	}

//	public void visitEnd() {
//		StringBuffer sb = new StringBuffer();
//		sb.append("visitEnd").append(": ");
//		System.out.println(sb.toString());
//	}
	
	
//	private void addMethodDesc(String desc) {
//		addType(Type.getReturnType(desc));
//		
//		final Type[] types = Type.getArgumentTypes(desc);
//		
//		for (int i = 0; i < types.length; i++) {
//			addType(types[i]);
//		}
//	}
//
//	private void addType(Object tt) {
//		if (!(tt instanceof Type)) {
//			return;
//		}
//		
//		final Type t = (Type)tt;
//		switch (t.getSort()) {
//			case Type.ARRAY:
//				addType(t.getElementType());
//				break;
//			case Type.OBJECT:
//				this.visitDependency(t.getClassName());
//				break;
//		}
//	}
}
