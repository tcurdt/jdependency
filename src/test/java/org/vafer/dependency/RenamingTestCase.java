package org.vafer.dependency;

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.vafer.dependency.asm.DelegatingVisitor;
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.utils.ResourceRenamer;

public class RenamingTestCase extends TestCase {

	public void testClass1Load() throws Exception {
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		
        final ClassWriter originalCw = new ClassWriter(true, false);
        new ClassReader(cl.getResourceAsStream("org/vafer/dependency/classes/Class1.class"))
        	.accept(originalCw, false);
        final Class originalClass = cl.loadClass(originalCw.toByteArray());
        
        assertEquals("org.vafer.dependency.classes.Class1", originalClass.getName());
	}

	public void testClass1Delegating() throws Exception {
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		
        final ClassWriter originalCw = new ClassWriter(true, false);
        new ClassReader(cl.getResourceAsStream("org/vafer/dependency/classes/Class1.class"))
    	.accept(new DelegatingVisitor(originalCw), false);
        final Class originalClass = cl.loadClass(originalCw.toByteArray());
        
        assertEquals("org.vafer.dependency.classes.Class1", originalClass.getName());
	}

	public void testClass1NoRename() throws Exception {
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		
        final ClassWriter originalCw = new ClassWriter(true, false);
        new ClassReader(cl.getResourceAsStream("org/vafer/dependency/classes/Class1.class"))
    	.accept(new RenamingVisitor(originalCw, new ResourceRenamer() {
			public String getNewResourceNameForResource(final String pOldName) {
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
        	.accept(new RenamingVisitor(renamedCw, new ResourceRenamer() {
				public String getNewResourceNameForResource(final String pOldName) {
					if (pOldName.startsWith("org.vafer.dependency.")) {
						return "my." + pOldName;
					}
					return pOldName;
				}        		
        	}), false);
        final Class renamedClass = cl.loadClass(renamedCw.toByteArray());

        assertEquals("my.org.vafer.dependency.classes.Class1", renamedClass.getName());
	}
}
