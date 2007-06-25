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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.vafer.dependency.classes.Main;
import org.vafer.dependency.classes.Reference;
import org.vafer.dependency.relocation.Jar;
import org.vafer.dependency.relocation.Processor;
import org.vafer.dependency.relocation.JarHandler;
import org.vafer.dependency.relocation.Version;

public class JarCombiningTestCase extends TestCase {
	
	private static class DefaultResourceHandler implements JarHandler {

		public void onStartProcessing(JarOutputStream pOutput) throws IOException {
		}

		public void onStartJar(Jar pJar, JarOutputStream pOutput) throws IOException {
		}

		public InputStream onResource(Jar pJar, String oldName, String newName, Version[] versions, InputStream inputStream) throws IOException {
			if ( pJar != versions[0].getJar() ) {
				// only process the first version of it
				return null;
			}

			return inputStream;
		}

		public void onStopJar(Jar pJar, JarOutputStream pOutput) throws IOException {
		}

		public void onStopProcessing(JarOutputStream pOutput) throws IOException {
		}		
	}
	
	private File createJar( final String[] pResources ) throws IOException {

		final File temp = File.createTempFile("jci", "jar");
		temp.deleteOnExit();
		
		final JarOutputStream output = new JarOutputStream(new FileOutputStream(temp));
		
		for (int i = 0; i < pResources.length; i++) {
			final JarEntry entry = new JarEntry(pResources[i]);
			
			final InputStream data = this.getClass().getClassLoader().getResourceAsStream(pResources[i]);
			
			if (data == null) {
				throw new IOException("Could not find resource " + pResources[i]);
			}
			
			output.putNextEntry(entry);
			
			IOUtils.copy(data, output);
			
			data.close();
		}

		output.close();
		
		return temp;
	}

	private Set listJar( final File pFile ) throws IOException {
		final JarInputStream input = new JarInputStream(new FileInputStream(pFile));
		final Set files = new HashSet();

		while(true) {
			final JarEntry entry = input.getNextJarEntry();
			
			if (entry == null) {
				break;
			}
			
			files.add(entry.getName());
		}

		input.close();
		
		return files;
	}

	private String classToResource( final String name ) {
		final String resource = name.replace('.', '/') + ".class";
		return resource;
	}
	
	private class ShieldingClassLoader extends URLClassLoader {

		public ShieldingClassLoader(URL[] urls) {
			super(urls, null, null);
		}

	}
	
	public void testMergeWithRelocate() throws Exception {
		
		final File jar1jar = createJar( new String[] { classToResource(Main.class.getName()) } );
		final File jar2jar = createJar( new String[] { classToResource(Reference.class.getName()) } );
		
		assertNotNull(jar1jar);
		assertNotNull(jar2jar);

		assertTrue(jar1jar.exists());
		assertTrue(jar2jar.exists());
				
		final Jar[] jars = new Jar[] {
				new Jar(jar1jar),
				new Jar(jar2jar, "jar2")
		};
		
		final File temp = File.createTempFile("jci", "jar");
		temp.deleteOnExit();

		final FileOutputStream output = new FileOutputStream(temp);
		
		final Processor processor = new Processor(new Console() {
			public void println(String pString) {
				System.out.println(pString);
			}						
		});
		
		processor.processJars(jars, new DefaultResourceHandler(), output);
		
		final Set files = listJar(temp);
		
		assertTrue(files.contains(classToResource(Main.class.getName())));
		assertTrue(files.contains("jar2/" + classToResource(Reference.class.getName())));
		assertTrue(files.contains("org/vafer/dependency/RuntimeMapper.class"));
		assertEquals(3, files.size()); // 2 + 1 mapper
		
		final URLClassLoader cl = new ShieldingClassLoader(new URL[] { temp.toURL() });
		final Class clazz = cl.loadClass(Main.class.getName());
		
		final Runnable run = (Runnable) clazz.newInstance();
		run.run();
	}

	public void testMergeWithoutRelocate() throws Exception {

		final File jar1jar = createJar( new String[] { classToResource(Main.class.getName()) } );
		final File jar2jar = createJar( new String[] { classToResource(Reference.class.getName()) } );
		
		assertNotNull(jar1jar);
		assertNotNull(jar2jar);

		assertTrue(jar1jar.exists());
		assertTrue(jar2jar.exists());
				
		final Jar[] jars = new Jar[] {
				new Jar(jar1jar),
				new Jar(jar2jar)
		};

		final File temp = File.createTempFile("jci", "jar");
		temp.deleteOnExit();
		
		final FileOutputStream output = new FileOutputStream(temp);
		
		final Processor processor = new Processor(new Console() {
			public void println(String pString) {
				System.out.println(pString);
			}						
		});
		
		processor.processJars(jars, new DefaultResourceHandler(), output);
		
		final Set files = listJar(temp);
		
		assertTrue(files.contains(classToResource(Main.class.getName())));
		assertTrue(files.contains(classToResource(Reference.class.getName())));
		assertFalse(files.contains("org/vafer/dependency/RuntimeMapper.class"));
		assertEquals(2, files.size());

		final URLClassLoader cl = new ShieldingClassLoader(new URL[] { temp.toURL() });
		final Class clazz = cl.loadClass(Main.class.getName());

		final Runnable run = (Runnable) clazz.newInstance();
		run.run();
	}

}
