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
import org.vafer.dependency.utils.ResourceRenamer;


public final class RenamingAdapter extends DelegatingAdapter {

    private String oldName;
    private String newName;

    public RenamingAdapter( final ClassVisitor pClassVisitor, final ResourceRenamer pResourceRenamer ) {        
        super(pClassVisitor);
    }

    /**
     * @deprecated
     */
    public RenamingAdapter( final ClassVisitor pClassVisitor, final String pNewName, final String pOldName ) {
        super(pClassVisitor);
        newName = toInternalName(pNewName);
        oldName = toInternalName(pOldName);
    }

    private String toInternalName( final String pName ) {
        if (pName.indexOf('.') >= 0) {
            return "L" + pName.replace('.', '/');
        }
        int lastCharIndex = pName.length() - 1;
        if (pName.charAt(lastCharIndex) == ';') {
            return pName.substring(0, lastCharIndex);
        }
        return pName;
    }

    private String transplant( final String pString ) {
        if (pString == null) {
            return null;
        }
        int index = pString.indexOf(oldName);
        if (index < 0) {
            return pString;
        }
        int lengthOld = oldName.length();
        StringBuffer result = new StringBuffer();
        String remaining = pString;
        while (remaining.length() > 0) {
            result.append(remaining.substring(0, index));
            char nextChar = remaining.charAt(index + lengthOld);
            if (nextChar == ';' || nextChar == '<' || nextChar == '>') {
                result.append(newName);
            } else {
                result.append(oldName);
            }
            remaining = remaining.substring(index + lengthOld);
            index = remaining.indexOf(oldName);
            if (index < 0) {
                result.append(remaining);
                break;
            }
        }
        return result.toString();
    }

    private String[] transplant( final String[] pStrings) {
        if (pStrings == null) {
            return null;
        }
        String[] result = new String[pStrings.length];
        for (int i = 0; i < pStrings.length; i++) {
            result[i] = transplant(pStrings[i]);
        }
        return result;
    }

    protected AnnotationVisitor doVisitClassAnnotation(String desc, boolean visible) {
        return super.doVisitClassAnnotation(transplant(desc), visible);
    }

    protected FieldVisitor doVisitField(int access, String name, String desc, String signature, Object value) {
        return super.doVisitField(access, name, transplant(desc), transplant(signature), value);
    }

    protected MethodVisitor doVisitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        return super.doVisitMethod(access, transplant(name), transplant(desc), transplant(signature), transplant(exceptions));
    }

    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, transplant(name), transplant(signature), transplant(superName), transplant(interfaces));
    }

    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode, transplant(owner), transplant(name), transplant(desc));
    }

    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(transplant(name), transplant(outerName), transplant(innerName), access);
    }

    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(transplant(name), transplant(desc), transplant(signature), start, end, index);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        super.visitMethodInsn(opcode, transplant(owner), transplant(name), transplant(desc));
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        super.visitMultiANewArrayInsn(transplant(desc), dims);
    }

    public void visitOuterClass(String owner, String name, String desc) {
        super.visitOuterClass(transplant(owner), transplant(name), transplant(desc));
    }

    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        super.visitTryCatchBlock(start, end, handler, transplant(type));
    }

    public void visitTypeInsn(int opcode, String desc) {
        super.visitTypeInsn(opcode, transplant(desc));
    }    
}
