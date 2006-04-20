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
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

public final class DependencyVisitor implements
        AnnotationVisitor,
        SignatureVisitor,
        ClassVisitor,
        FieldVisitor,
        MethodVisitor
{
    
    final Set classes = new HashSet();
    
    public Set getDependencies() {
        return classes;
    }
    
    public void visit(
        int version,
        int access,
        String name,
        String signature,
        String superName,
        String[] interfaces)
    {
        if (signature == null) {
            addName(superName);
            addNames(interfaces);
        } else {
            addSignature(signature);
        }
    }

    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        addDesc(desc);
        return this;
    }

    public void visitAttribute(Attribute attr) {
    }

    public FieldVisitor visitField(
        int access,
        String name,
        String desc,
        String signature,
        Object value
        )
    {
        if (signature == null) {
            //addDesc(desc);
        } else {
            addTypeSignature(signature);
        }
        if (value instanceof Type) {
            addType((Type) value);
        }
        return this;
    }

    public MethodVisitor visitMethod(
        int access,
        String name,
        String desc,
        String signature,
        String[] exceptions
        )
    {
        if (signature == null) {
            addMethodDesc(desc);
        } else {
            addSignature(signature);
        }
        addNames(exceptions);
        return this;
    }

    public void visitSource(String source, String debug) {
    }

    public void visitInnerClass(
        String name,
        String outerName,
        String innerName,
        int access
        )
    {
        //addName(innerName);
        addName(outerName);
    }

    public void visitOuterClass(String owner, String name, String desc) {
        addName(owner);
        if (desc != null) {
            addMethodDesc(desc);
        }
    }

    
    final static String[] search = {
        "I", "Ljava/io/File;" 
    };
    
    private void bark( final String s ) {
        if (s == null) {
            return;
        }
        
        for (int i = 0; i < search.length; i++) {
            if (s.equals(search[i])) {
                throw new RuntimeException(s);
            }            
        }
    }

    // MethodVisitor

    public AnnotationVisitor visitParameterAnnotation(
        int parameter,
        String desc,
        boolean visible
        )
    {
        addDesc(desc);
        return this;
    }

    public void visitTypeInsn(int opcode, String desc) {
        addDesc(desc);
    }

    public void visitFieldInsn(
        int opcode,
        String owner,
        String name,
        String desc
        )
    {
        addName(owner);
        //addName(name);
        //addDesc(desc);
    }

    public void visitMethodInsn(
        int opcode,
        String owner,
        String name,
        String desc
        )
    {
        addName(owner);
        //addName(name);
        addMethodDesc(desc);
    }

    public void visitLdcInsn(Object cst) {
        if (cst instanceof Type) {
            addType((Type) cst);
        }
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        addDesc(desc);
    }

    public void visitLocalVariable(
        String name,
        String desc,
        String signature,
        Label start,
        Label end,
        int index
        )
    {
        //addName(name);
        //addDesc(desc);
        addTypeSignature(signature);
    }

    public AnnotationVisitor visitAnnotationDefault() {
        return this;
    }

    public void visitCode() {
    }

    public void visitInsn(int opcode) {
    }

    public void visitIntInsn(int opcode, int operand) {
    }

    public void visitVarInsn(int opcode, int var) {
    }

    public void visitJumpInsn(int opcode, Label label) {
    }

    public void visitLabel(Label label) {
    }

    public void visitIincInsn(int var, int increment) {
    }

    public void visitTableSwitchInsn(
        int min,
        int max,
        Label dflt,
        Label[] labels
        )
    {
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    }

    public void visitTryCatchBlock(
        Label start,
        Label end,
        Label handler,
        String type
        )
    {
        addName(type);
    }

    public void visitLineNumber(int line, Label start) {
    }

    public void visitMaxs(int maxStack, int maxLocals) {
    }

    public void visit(String name, Object value) {
        addName(name);
        if (value instanceof Type) {
            addType((Type) value);
        }
    }

    public void visitEnum(String name, String desc, String value) {
        addName(name);        
        addDesc(desc);
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
        addName(name);
        addDesc(desc);
        return this;
    }

    public AnnotationVisitor visitArray(String name) {
        addName(name);
        return this;
    }

    public void visitFormalTypeParameter(String name) {
        addName(name);
    }

    public SignatureVisitor visitClassBound() {
        return this;
    }

    public SignatureVisitor visitInterfaceBound() {
        return this;
    }

    public SignatureVisitor visitSuperclass() {
        return this;
    }

    public SignatureVisitor visitInterface() {
        return this;
    }

    public SignatureVisitor visitParameterType() {
        return this;
    }

    public SignatureVisitor visitReturnType() {
        return this;
    }

    public SignatureVisitor visitExceptionType() {
        return this;
    }

    public void visitBaseType(char descriptor) {
    }

    public void visitTypeVariable(String name) {
        addName(name);
    }

    public SignatureVisitor visitArrayType() {
        return this;
    }

    public void visitClassType(String name) {
        addName(name);
    }

    public void visitInnerClassType(String name) {
        addName(name);
    }

    public void visitTypeArgument() {
    }

    public SignatureVisitor visitTypeArgument(char wildcard) {
        return this;
    }

    public void visitEnd() {
    }

    // ---------------------------------------------

    private void addName(String name) {
        if (name == null) {
            return;
        }

        bark(name);
        
        classes.add(name.replace('/', '.'));
    }

    private void addNames(String[] names) {
        for (int i = 0; names != null && i < names.length; i++)
            addName(names[i]);
    }

    private void addDesc(String desc) {
        if (desc.charAt(0) == '[') {
            addType(Type.getType(desc));
        } else {
            addName(desc);
        }
    }

    private void addMethodDesc(String desc) {
        addType(Type.getReturnType(desc));
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; i++)
            addType(types[i]);
    }

    private void addType(Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                addType(t.getElementType());
                break;
            case Type.OBJECT:
                addName(t.getClassName().replace('.', '/'));
                break;
        }
    }

    private void addSignature(String signature) {
        if (signature != null)
            new SignatureReader(signature).accept(this);
    }

    private void addTypeSignature(String signature) {
        if (signature != null)
            new SignatureReader(signature).acceptType(this);
    }
}
