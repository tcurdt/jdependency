package org.vafer.dependency;

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.vafer.dependency.asm.DelegatingVisitor;
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.utils.ResourceRenamer;

public class RenamingTestCase extends TestCase {

	public void testClass1Load() throws Exception {
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		
        final ClassWriter originalCw = new ClassWriter(true, false);
        new ClassReader(cl.getResourceAsStream("org/vafer/dependency/classes/Class1.class"))
        	.accept(new CheckClassAdapter(originalCw), false);
        final Class originalClass = cl.loadClass(originalCw.toByteArray());
        
        assertEquals("org.vafer.dependency.classes.Class1", originalClass.getName());
	}

	public void testClass1Delegating() throws Exception {
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		
        final ClassWriter originalCw = new ClassWriter(true, false);
        new ClassReader(cl.getResourceAsStream("org/vafer/dependency/classes/Class1.class"))
    	.accept(new DelegatingVisitor(new CheckClassAdapter(originalCw)), false);
        final Class originalClass = cl.loadClass(originalCw.toByteArray());
        
        assertEquals("org.vafer.dependency.classes.Class1", originalClass.getName());
	}

	public void testClass1NamePassThrough() throws Exception {
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		
        final ClassWriter originalCw = new ClassWriter(true, false);
        new ClassReader(cl.getResourceAsStream("org/vafer/dependency/classes/Class1.class"))
    	.accept(new RenamingVisitor(new CheckClassAdapter(originalCw), new ResourceRenamer() {
			public String getNewNameFor(final String pOldName) {
				return pOldName;
			}        		
    	}), false);
        final Class originalClass = cl.loadClass(originalCw.toByteArray());
        
        assertEquals("org.vafer.dependency.classes.Class1", originalClass.getName());
	}
	
    public void testClass1Rename() throws Exception {
    	final BytecodeClassLoader cl = new BytecodeClassLoader();
    	
        final ClassWriter renamedCw = new ClassWriter(true, false);
        new ClassReader(cl.getResourceAsStream("org/vafer/dependency/classes/Class1.class"))
        	.accept(new RenamingVisitor(new CheckClassAdapter(renamedCw), new ResourceRenamer() {
				public String getNewNameFor(final String pOldName) {
					if (pOldName.startsWith("org.vafer.dependency.")) {
						return "my." + pOldName;
					}
					return pOldName;
				}        		
        	}), false);
        final Class renamedClass = cl.loadClass(renamedCw.toByteArray());

        assertEquals("my.org.vafer.dependency.classes.Class1", renamedClass.getName());
	}

    public void testHashMapRename() throws Exception {
    	final BytecodeClassLoader cl = new BytecodeClassLoader();
    	
        final ClassWriter renamedCw = new ClassWriter(true, false);
        new ClassReader(cl.getResourceAsStream("java/util/HashMap.class"))
        	.accept(new RenamingVisitor(new CheckClassAdapter(renamedCw), new ResourceRenamer() {
				public String getNewNameFor(final String pOldName) {
					if (pOldName.startsWith("java.util.HashMap")) {
						return "my." + pOldName;
					}
					return pOldName;
				}        		
        	}), false);
        final Class renamedClass = cl.loadClass(renamedCw.toByteArray());

        assertEquals("my.java.util.HashMap", renamedClass.getName());
	}
}
