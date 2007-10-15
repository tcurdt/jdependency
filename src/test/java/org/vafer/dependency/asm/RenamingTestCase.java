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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.util.CheckClassAdapter;

public final class RenamingTestCase extends TestCase {

	public final static class Test {		
	}
	
	private final static String name = Test.class.getName();
	private final static String resource = Test.class.getName().replace('.', '/') + ".class";
	
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

	
	
	public void testClass1Load() throws Exception {
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		
        final ClassWriter originalCw = new ClassWriter(0);
        new ClassReader(getClass().getClassLoader().getResourceAsStream(resource))
        	.accept(new CheckClassAdapter(originalCw), 0);
        final Class originalClass = cl.loadClass(originalCw.toByteArray());
        
        assertEquals(name, originalClass.getName());
	}

	public void testClass1NamePassThrough() throws Exception {
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		
        final ClassWriter originalCw = new ClassWriter(0);
        new ClassReader(getClass().getClassLoader().getResourceAsStream(resource))
    	.accept(new RemappingClassAdapter(new CheckClassAdapter(originalCw), new Remapper() {
    		public String map( final String pOldName ) {
    			return pOldName;
    		}
    	}), 0);
        final Class originalClass = cl.loadClass(originalCw.toByteArray());
        
        assertEquals(name, originalClass.getName());
	}
	
    public void testClass1Rename() throws Exception {
    	final BytecodeClassLoader cl = new BytecodeClassLoader();
    	
        final ClassWriter renamedCw = new ClassWriter(0);
        new ClassReader(getClass().getClassLoader().getResourceAsStream(resource))
        	.accept(new RemappingClassAdapter(new CheckClassAdapter(renamedCw), new Remapper() {
	    		public String map( final String pOldName ) {
					if (pOldName.startsWith("org/vafer/dependency/")) {
						return "my/" + pOldName;
					}
					return pOldName;
	    		}
        	}), 0);
        final Class renamedClass = cl.loadClass(renamedCw.toByteArray());

        assertEquals("my." + name, renamedClass.getName());
	}

    public void testHashMapRename() throws Exception {
    	final BytecodeClassLoader cl = new BytecodeClassLoader();
    	
        final ClassWriter renamedCw = new ClassWriter(0);
        new ClassReader(getClass().getClassLoader().getResourceAsStream("java/util/HashMap.class"))
        	.accept(new RemappingClassAdapter(new CheckClassAdapter(renamedCw), new Remapper() {
	    		public String map( final String pOldName ) {
					if (pOldName.startsWith("java/util/HashMap")) {
						return "my/" + pOldName;
					}
					return pOldName;
	    		}
        	}), 0);
        final Class renamedClass = cl.loadClass(renamedCw.toByteArray());

        assertEquals("my.java.util.HashMap", renamedClass.getName());
        
	}

    
    
	public static class FileInputStreamProxy extends InputStream {
		
		private final InputStream in = new ByteArrayInputStream((
				"public class Test {" +
				"public static void main(String[] args) {} }"
				).getBytes());
		
		public FileInputStreamProxy(File file) throws FileNotFoundException {
//			System.out.println("Reading from file " + file);
		}

		public FileInputStreamProxy(FileDescriptor fdObj) {
		}

		public FileInputStreamProxy(String name) throws FileNotFoundException {
		}
		
		public int read() throws IOException {
			return in.read();
		}

		public int available() throws IOException {
			return in.available();			
		}

		public void close() throws IOException {
			in.close();
		}

		public synchronized void mark(int readlimit) {
			in.mark(readlimit);
		}

		public boolean markSupported() {
			return in.markSupported();
		}

		public int read(byte[] b, int off, int len) throws IOException {
			return in.read(b, off, len);
		}

		public int read(byte[] b) throws IOException {
			return in.read(b);
		}

		public synchronized void reset() throws IOException {
			in.reset();
		}

		public long skip(long n) throws IOException {
			return in.skip(n);
		}
	};
	
	public static class FileOutputStreamProxy extends OutputStream {
		
		final private ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		public FileOutputStreamProxy(File file, boolean append) throws FileNotFoundException {
		}
	
		public FileOutputStreamProxy(File file) throws FileNotFoundException {
//			System.out.println("Writing to file " + file);
		}
	
		public FileOutputStreamProxy(FileDescriptor fdObj) {
		}
	
		public FileOutputStreamProxy(String name, boolean append) throws FileNotFoundException {
		}
	
		public FileOutputStreamProxy(String name) throws FileNotFoundException {
		}
		
		public void write(int value) throws IOException {
			out.write(value);
		}

		public void close() throws IOException {
//			System.out.println("Wrote " + out.size() + " bytes");
			out.close();
		}

		public void flush() throws IOException {
			out.flush();
		}

		public void write(byte[] b, int off, int len) throws IOException {
			out.write(b, off, len);
		}

		public void write(byte[] b) throws IOException {
			out.write(b);
		}
	};

    
    public void testJavacInputRename() throws Exception {

    	final ClassLoader cl = new ClassLoader(getClass().getClassLoader()) {

			protected Class findClass(final String name) throws ClassNotFoundException {

				if (name.startsWith("java.")) {
					return super.findClass(name);
				}
				
				InputStream classStream = getResourceAsStream(name.replace('.', '/') + ".class");
				
				try {
					
					final byte[] classBytes;

					if (name.startsWith("")) {
				        final ClassWriter renamedCw = new ClassWriter(0);
				        new ClassReader(classStream).accept(new RemappingClassAdapter(new CheckClassAdapter(renamedCw), new Remapper() {
				        	public String map(final String pOldName) {
				        		
								if (pOldName.startsWith(FileOutputStream.class.getName().replace('.', '/'))) {
									return FileOutputStreamProxy.class.getName().replace('.', '/');
								}
								if (pOldName.startsWith(FileInputStream.class.getName().replace('.', '/'))) {
									return FileInputStreamProxy.class.getName().replace('.', '/');
								}
								return pOldName;
							}        		
			        	}), 0);

			        	classBytes = renamedCw.toByteArray();
						
					} else {
						classBytes = IOUtils.toByteArray(classStream);						
					}
					
					return defineClass(name, classBytes, 0, classBytes.length);
				} catch (IOException e) {
					throw new ClassNotFoundException("", e);
				}
			}

			protected synchronized Class loadClass(String classname, boolean resolve) throws ClassNotFoundException {
				
			       Class theClass = findLoadedClass(classname);
			        if (theClass != null) {
			            return theClass;
			        }

		            try {
		                theClass = findClass(classname);
		            } catch (ClassNotFoundException cnfe) {
		                theClass = getParent().loadClass(classname);
		            }

			        if (resolve) {
			            resolveClass(theClass);
			        }

			        return theClass;
			}
    	};
    	
        final Class renamedClass = cl.loadClass("com.sun.tools.javac.Main");

		final Method compile = renamedClass.getMethod("compile", new Class[] { String[].class, PrintWriter.class });
		final StringWriter out = new StringWriter();
		Integer ok = (Integer) compile.invoke(null, new Object[] { new String[]{ "/Users/tcurdt/Test.java" }, new PrintWriter(out) });
		
		assertEquals(ok.intValue(), 0);
	}

}
