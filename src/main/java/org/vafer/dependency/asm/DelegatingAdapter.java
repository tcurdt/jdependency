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
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public abstract class DelegatingAdapter implements ClassVisitor, FieldVisitor, MethodVisitor {

    private ClassVisitor cv;
    private MethodVisitor mv;
    private FieldVisitor fv;


    protected DelegatingAdapter(final ClassVisitor pClassVisitor) {
        cv = pClassVisitor;
    }


    public final AnnotationVisitor visitAnnotation( String desc, boolean visible ) {
        if (fv != null) {
            return doVisitFieldAnnotation(desc, visible);
        }
        if (mv != null) {
            return doVisitMethodAnnotation(desc, visible);
        }
        return doVisitClassAnnotation(desc, visible);
    }


    protected AnnotationVisitor doVisitClassAnnotation( String desc, boolean visible ) {
        return cv.visitAnnotation(desc, visible);
    }


    protected AnnotationVisitor doVisitMethodAnnotation( String desc, boolean visible ) {
        return mv.visitAnnotation(desc, visible);
    }


    protected AnnotationVisitor doVisitFieldAnnotation( String desc, boolean visible ) {
        return fv.visitAnnotation(desc, visible);
    }


    public final void visitAttribute( Attribute attr ) {
        if (fv != null) {
            doVisitFieldAttribute(attr);
        } else if (mv != null) {
            doVisitMethodAttribute(attr);
        } else {
            doVisitClassAttribute(attr);
        }
    }


    protected void doVisitClassAttribute( Attribute attr ) {
        cv.visitAttribute(attr);
    }


    protected void doVisitMethodAttribute( Attribute attr ) {
        mv.visitAttribute(attr);
    }


    protected void doVisitFieldAttribute( Attribute attr ) {
        fv.visitAttribute(attr);
    }


    public final void visitEnd() {
        if (fv != null) {
            doVisitFieldEnd();
            fv = null;
        } else if (mv != null) {
            doVisitMethodEnd();
            mv = null;
        } else {
            doVisitClassEnd();
        }
    }


    protected void doVisitClassEnd() {
        cv.visitEnd();
    }


    protected void doVisitMethodEnd() {
        mv.visitEnd();
    }


    protected void doVisitFieldEnd() {
        fv.visitEnd();
    }


    public void visit( int version, int access, String name, String signature, String superName,
            String[] interfaces ) {
        cv.visit(version, access, name, signature, superName, interfaces);
    }


    public final FieldVisitor visitField( int access, String name, String desc, String signature,
            Object value ) {
        fv = doVisitField(access, name, desc, signature, value);
        return this;
    }


    protected FieldVisitor doVisitField( int access, String name, String desc, String signature,
            Object value ) {
        return cv.visitField(access, name, desc, signature, value);
    }


    public void visitInnerClass( String name, String outerName, String innerName, int access ) {
        cv.visitInnerClass(name, outerName, innerName, access);
    }


    public final MethodVisitor visitMethod( int access, String name, String desc, String signature,
            String[] exceptions ) {
        mv = doVisitMethod(access, name, desc, signature, exceptions);
        return this;
    }


    protected MethodVisitor doVisitMethod( int access, String name, String desc, String signature,
            String[] exceptions ) {
        return cv.visitMethod(access, name, desc, signature, exceptions);
    }


    public void visitOuterClass( String owner, String name, String desc ) {
        cv.visitOuterClass(owner, name, desc);
    }


    public void visitSource( String source, String debug ) {
        cv.visitSource(source, debug);
    }


    public AnnotationVisitor visitAnnotationDefault() {
        return mv.visitAnnotationDefault();
    }


    public void visitCode() {
        mv.visitCode();
    }


    public void visitFieldInsn( int opcode, String owner, String name, String desc ) {
        mv.visitFieldInsn(opcode, owner, name, desc);
    }


    public void visitIincInsn( int var, int increment ) {
        mv.visitIincInsn(var, increment);
    }


    public void visitInsn( int opcode ) {
        mv.visitInsn(opcode);
    }


    public void visitIntInsn( int opcode, int operand ) {
        mv.visitIntInsn(opcode, operand);
    }


    public void visitJumpInsn( int opcode, Label label ) {
        mv.visitJumpInsn(opcode, label);
    }


    public void visitLabel( Label label ) {
        mv.visitLabel(label);
    }


    public void visitLdcInsn( Object cst ) {
        mv.visitLdcInsn(cst);
    }


    public void visitLineNumber( int line, Label start ) {
        mv.visitLineNumber(line, start);
    }


    public void visitLocalVariable( String name, String desc, String signature, Label start,
            Label end, int index ) {
        mv.visitLocalVariable(name, desc, signature, start, end, index);
    }


    public void visitLookupSwitchInsn( Label dflt, int[] keys, Label[] labels ) {
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }


    public void visitMaxs( int maxStack, int maxLocals ) {
        mv.visitMaxs(maxStack, maxLocals);
    }


    public void visitMethodInsn( int opcode, String owner, String name, String desc ) {
        mv.visitMethodInsn(opcode, owner, name, desc);
    }


    public void visitMultiANewArrayInsn( String desc, int dims ) {
        mv.visitMultiANewArrayInsn(desc, dims);
    }


    public AnnotationVisitor visitParameterAnnotation( int parameter, String desc, boolean visible ) {
        return mv.visitParameterAnnotation(parameter, desc, visible);
    }


    public void visitTableSwitchInsn( int min, int max, Label dflt, Label[] labels ) {
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }


    public void visitTryCatchBlock( Label start, Label end, Label handler, String type ) {
        mv.visitTryCatchBlock(start, end, handler, type);
    }


    public void visitTypeInsn( int opcode, String desc ) {
        mv.visitTypeInsn(opcode, desc);
    }


    public void visitVarInsn( int opcode, int var ) {
        mv.visitVarInsn(opcode, var);
    }
}
