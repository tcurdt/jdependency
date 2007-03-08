package org.vafer.dependency;

import java.io.InputStream;

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.vafer.dependency.asm.ResourceLookupWrappingClassTransformer;

public class ResourceLookupTestCase extends TestCase {

	
	private void rewriteClass( final Class pClass ) throws Exception {
		final String resource = "/" + pClass.getName().replace('.', '/') + ".class";
		final InputStream is = pClass.getResourceAsStream(resource);
        final ClassReader r = new ClassReader(is);
        final ClassWriter w = new ClassWriter(true);

        final ResourceLookupWrappingClassTransformer t = new ResourceLookupWrappingClassTransformer(new Console() {
			public void println(String pString) {
				System.out.println(pString);
			}        	
        });
        
        r.accept(t.new WrappingClassAdapter(w), false);
	}
	
	
	public static class Test1 {
		public void instanceMethod() throws Exception {
			final Class c = Class.forName("name1");
			Class.forName("name2");
			final String s = "name3";
			Class.forName(s);
		}
		
		public static void staticMethod() {
		}
	}
	
	public void testClassForName() throws Exception {
		rewriteClass(Test1.class);
	}
	
}
