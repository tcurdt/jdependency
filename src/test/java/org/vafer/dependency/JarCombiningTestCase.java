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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

import junit.framework.TestCase;

import org.vafer.dependency.resources.DefaultResourceHandler;
import org.vafer.dependency.resources.Version;
import org.vafer.dependency.utils.Jar;
import org.vafer.dependency.utils.JarUtils;

public class JarCombiningTestCase extends TestCase {
	
	public void testMergeWithRelocate() throws Exception {
		
		final URL jar1jar = this.getClass().getClassLoader().getResource("jar1.jar");		
		final URL jar2jar = this.getClass().getClassLoader().getResource("jar2.jar");

		assertNotNull(jar1jar);
		assertNotNull(jar2jar);
				
		final Jar[] jars = new Jar[] {
				new Jar(new File(jar1jar.toURI()), true ) {
					public String getRelocatePrefix() {
						return "jar1/";
					}					
				},
				new Jar(new File(jar2jar.toURI()), true ) {
					public String getRelocatePrefix() {
						return "jar2/";
					}					
				}
		};
		
		final File temp = File.createTempFile("jci", "jar");
		temp.deleteOnExit();

		final FileOutputStream out = new FileOutputStream(temp);
		
		JarUtils.processJars(jars, new DefaultResourceHandler(), out, new Console() {
			public void println(String pString) {
				System.out.println(pString);
			}			
		});
	}

	public void testMergeWithoutRelocate() throws Exception {

		final URL jar1jar = this.getClass().getClassLoader().getResource("jar1.jar");		
		final URL jar2jar = this.getClass().getClassLoader().getResource("jar2.jar");

		assertNotNull(jar1jar);
		assertNotNull(jar2jar);
				
		final Jar[] jars = new Jar[] {
				new Jar(new File(jar1jar.toURI()), false),
				new Jar(new File(jar2jar.toURI()), false)
		};

		final File temp = File.createTempFile("jci", "jar");
		temp.deleteOnExit();
		
		final FileOutputStream out = new FileOutputStream(temp);

		
		JarUtils.processJars(
				jars,
				new DefaultResourceHandler() {

					public InputStream onResource(Jar jar, String oldName, String newName, Version[] versions, InputStream inputStream) {
						if ( jar != versions[0].getJar() )
						{
							// only process the first version of it
							return null;
						}
						
						return inputStream;
					}
					
				},
				out,
				new Console() {
					public void println(String pString) {
						System.out.println(pString);
				}			
			});
	}

}
