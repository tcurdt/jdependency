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
import java.net.URL;

import junit.framework.TestCase;

import org.vafer.dependency.utils.Jar;
import org.vafer.dependency.utils.JarUtils;

public class JarCombiningTestCase extends TestCase {
	
	public void testMergeWithRelocate() throws Exception {
		
		final URL jar1jar = this.getClass().getClassLoader().getResource("jar1.jar");		
		final URL jar2jar = this.getClass().getClassLoader().getResource("jar2.jar");

		assertNotNull(jar1jar);
		assertNotNull(jar2jar);
				
		final Jar[] jars = new Jar[] {
				new Jar(new File(jar1jar.toURI())) {
					public String getNewNameFor(String name) {
						return "jar1/" + name;
					}					
				},
				new Jar(new File(jar2jar.toURI())) {
					public String getNewNameFor(String name) {
						return "jar2/" + name;
					}					
				}
		};
		
		final FileOutputStream out = new FileOutputStream("out1.jar");
		
		JarUtils.processJars(jars, out, new Console() {
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
				new Jar(new File(jar1jar.toURI())),
				new Jar(new File(jar2jar.toURI()))
		};

		final FileOutputStream out = new FileOutputStream("out2.jar");

		
		JarUtils.processJars(jars, out, new Console() {
			public void println(String pString) {
				System.out.println(pString);
			}			
		});
	}

}
