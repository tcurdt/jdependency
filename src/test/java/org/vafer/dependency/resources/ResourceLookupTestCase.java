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
package org.vafer.dependency.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.vafer.dependency.asm.Remapper;
import org.vafer.dependency.resources.buildtime.BuildtimeResourceResolver;
import org.vafer.dependency.resources.runtime.MapperDump;
import org.vafer.dependency.resources.runtime.RuntimeResourceResolver;
import org.vafer.jar.AbstractJarTestCase;
import org.vafer.jar.Jar;
import org.vafer.jar.JarProcessor;
import org.vafer.jar.handler.JarHandler;
import org.vafer.jar.handler.OutputJarHandler;
import org.vafer.jar.handler.RenamingJarHandler;

public class ResourceLookupTestCase extends AbstractJarTestCase {

	private static final class BytecodeClassLoader extends ClassLoader {

	    public BytecodeClassLoader() {
			super();
		}

		public BytecodeClassLoader(ClassLoader parent) {
			super(parent);
		}

		public Class loadClass( final byte[] bytecode ) {

			final Class clazz = defineClass(null, bytecode, 0, bytecode.length);
	        
	        return clazz;
	    }
	}

	public void testStaticResourceResolver() throws Exception {

		final File jar1 = createJar(this.getClass(),
				new String[] { classToResource(Lookup.class.getName()) }
		);

		final File output = File.createTempFile("output", "jar");
		
		final JarHandler handler = new RenamingJarHandler(
				new RenamingJarHandler.Mapper() {
					public String getNameFor(Jar jar, String name) {
						return "prefix/" + name;
					}			
				}, new BuildtimeResourceResolver()).chain(new OutputJarHandler(new FileOutputStream(output)));
		
		new JarProcessor().processJars(new Jar[] { new Jar(jar1) }, handler);

		output.delete();		
	}

	public void testRuntimeResourceResolver() throws Exception {

		final File jar1 = createJar(this.getClass(),
				new String[] { classToResource(Lookup.class.getName()) }
		);

		final File output = File.createTempFile("output", "jar");
		
		final JarHandler handler = new RenamingJarHandler(
				new RenamingJarHandler.Mapper() {
					public String getNameFor(Jar jar, String name) {
						return "prefix/" + name;
					}			
				}, new RuntimeResourceResolver()).chain(new OutputJarHandler(new FileOutputStream(output)));
		
		new JarProcessor().processJars(new Jar[] { new Jar(jar1) }, handler);
		
		output.delete();		
	}
	
	
	public void testRuntimeRewriting() throws Exception {

//		{
//			final InputStream original = Lookup.class.getClassLoader().getResourceAsStream(classToResource(Lookup.class.getName()));
//			assertNotNull(original);		
//			new ClassReader(original).accept(new TraceClassVisitor(new PrintWriter(System.out)),0);
//		}

		final InputStream original = Lookup.class.getClassLoader().getResourceAsStream(classToResource(Lookup.class.getName()));
		assertNotNull(original);		

		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    
		final Remapper remapper =
			new Remapper() {
				public String map( final String name ) {
					return "prefix/" + name;
				}
		};

		ClassAdapter adapter = new CheckClassAdapter(cw);

		adapter = new RuntimeResourceResolver().getClassAdapter(cw, remapper);

		new ClassReader(original).accept(adapter,0);
		
		final byte[] classBytes = cw.toByteArray();
//		final InputStream rewritten = new ByteArrayInputStream(classBytes);
//		new ClassReader(rewritten).accept(new TraceClassVisitor(new PrintWriter(System.out)),0);

		final BytecodeClassLoader cl = new BytecodeClassLoader();
		final Class c = cl.loadClass(classBytes);
		final Method m = c.getMethod("find", null);
		final Object r = m.invoke(c.newInstance(), new Object[] { });
		
		assertNotNull(r);
	}
	
	public void testResolveBehaviour() throws Exception {
		
		final Map mapping = new HashMap();
		mapping.put("resource", "prefix/resource");
		
		final byte[] generatedClassBytes = MapperDump.dump("org/vafer/dependency/Mapper", mapping);
		
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		final Class c = cl.loadClass(generatedClassBytes);
		final Method m = c.getMethod("resolveResource", new Class[] { String.class });
		final Object r = m.invoke(null, new Object[] { "resource" });
		
		assertEquals("prefix/resource", r);
	}
}
