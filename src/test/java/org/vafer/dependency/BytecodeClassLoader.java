package org.vafer.dependency;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;


public final class BytecodeClassLoader extends ClassLoader {

    class NameClassAdapter extends ClassAdapter {
        private String className;

        public NameClassAdapter() {
            super(new EmptyVisitor());
        }
        
        public void visit( int version, int access, String name, String signature, String superName, String[] interfaces ) {
            className = name;
        }
        
        public String getName() {
            return className;
        }
    }
    
    public Class loadClass( final byte[] bytecode ) {
        final NameClassAdapter nameClassAdapter = new NameClassAdapter();
        new ClassReader(bytecode).accept(nameClassAdapter, false);
        final String name = nameClassAdapter.getName().replace('/', '.');        
        System.out.println("loading class " + name);
        
        final Class clazz = defineClass(null, bytecode, 0, bytecode.length);
        
        return clazz;
    }
}
