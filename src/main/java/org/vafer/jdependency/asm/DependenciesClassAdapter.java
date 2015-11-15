/*
 * Copyright 2010-2015 The jdependency developers.
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
package org.vafer.jdependency.asm;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

public final class DependenciesClassAdapter extends RemappingClassAdapter {

    private static final EmptyVisitor ev = new EmptyVisitor();

    public DependenciesClassAdapter() {
        super(ev, new CollectingRemapper());
    }

    public Set<String> getDependencies() {
        return ((CollectingRemapper) super.remapper).classes;
    }

    private static class CollectingRemapper extends Remapper {

        final Set<String> classes = new HashSet<String>();

        public String map(String pClassName) {
            classes.add(pClassName.replace('/', '.'));
            return pClassName;
        }
    }

    static class EmptyVisitor extends ClassVisitor {

        private static final AnnotationVisitor av = new AnnotationVisitor(Opcodes.ASM5) {

            public AnnotationVisitor visitAnnotation(String name, String desc) {
                return this;
            }

            public AnnotationVisitor visitArray(String name) {
                return this;
            }
        };

        private static final MethodVisitor mv = new MethodVisitor( Opcodes.ASM5) {

            public AnnotationVisitor visitAnnotationDefault() {
                return av;
            }

            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return av;
            }

            public AnnotationVisitor visitParameterAnnotation(
                int parameter, String desc, boolean visible) {
                return av;
            }
        };

        private static final FieldVisitor fieldVisitor = new FieldVisitor( Opcodes.ASM5 ) {
            public AnnotationVisitor visitAnnotation( String desc, boolean visible ) {
                return av;
            }
        };

        public EmptyVisitor() {
            super(Opcodes.ASM5);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            return av;
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            return fieldVisitor;
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            return mv;
        }
    }
}
