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
package org.vafer.dependency;

import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import junit.framework.TestCase;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.vafer.dependency.asm.RuntimeWrappingClassAdapter;
import org.vafer.dependency.classes.Resolve1;

public class ResourceLookupTestCase extends TestCase {

	private Class rewriteClass( final Class pClass ) throws Exception {
		final String resource = "/" + pClass.getName().replace('.', '/') + ".class";
		final InputStream is = pClass.getResourceAsStream(resource);
        final ClassReader r = new ClassReader(is);
        final ClassWriter w = new ClassWriter(true);

        final RuntimeWrappingClassAdapter t = new RuntimeWrappingClassAdapter(w, "org/vafer/dependency/classes/Mapper", new Console() {
			public void println(String pString) {
				System.out.println(pString);
			}        	
        });
        
        r.accept(t, false);
        
        // just make sure the classloader sees the class
        new BytecodeClassLoader(this.getClass().getClassLoader()).loadClass("org.vafer.dependency.classes.Mapper");
        
        return new BytecodeClassLoader(this.getClass().getClassLoader()).loadClass(w.toByteArray());        
	}
	
	
	public static class Test1 {
		public void instanceMethod() throws Exception {
			final Class c1 = Class.forName("name1");
			Class.forName("name2");
			final String s = "name3";
			Class.forName(s);			
			final URL u = this.getClass().getResource("name4");
			final InputStream is = this.getClass().getResourceAsStream("name5");
			final ClassLoader cl = this.getClass().getClassLoader();
			final Class c2 = cl.loadClass("name6");
		}
		
		public static void staticMethod() throws Exception {
			final URL u = ClassLoader.getSystemResource("name1");
			final InputStream is = ClassLoader.getSystemResourceAsStream("name2");
			final Enumeration en = ClassLoader.getSystemResources("name3");
		}
	}

	
	public void testClassForName() throws Exception {
		final Runnable r = (Runnable) rewriteClass(Resolve1.class).newInstance();
		r.run();
	}
	
}
