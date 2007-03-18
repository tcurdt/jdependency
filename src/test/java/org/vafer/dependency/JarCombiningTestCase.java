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

import org.vafer.dependency.utils.JarProcessor;
import org.vafer.dependency.utils.JarUtils;
import org.vafer.dependency.utils.JarUtils.DuplicateHandler;

public class JarCombiningTestCase extends TestCase {
	
	public void testMergeWithRelocate() throws Exception {
		
		final URL jar1jar = this.getClass().getClassLoader().getResource("jar1.jar");		
		final URL jar2jar = this.getClass().getClassLoader().getResource("jar2.jar");

		assertNotNull(jar1jar);
		assertNotNull(jar2jar);
				
		final JarProcessor[] jars = new JarProcessor[] {
				new JarProcessor(new File(jar1jar.toURI())),
				new JarProcessor(new File(jar2jar.toURI()))
		};

		final DuplicateHandler handler = new DuplicateHandler() {
			public JarProcessor handleDuplicate(String pName, JarProcessor[] jars) {
				return jars[0];
			}			
		};
		
		final FileOutputStream out = new FileOutputStream("out1.jar");
		
		boolean result = JarUtils.processJars(jars, handler, out, new Console() {
			public void println(String pString) {
				System.out.println(pString);
			}			
		});
		
		assertTrue(result);
		
	}

	public void testMergeWithoutRelocate() throws Exception {

		final URL jar1jar = this.getClass().getClassLoader().getResource("jar1.jar");		
		final URL jar2jar = this.getClass().getClassLoader().getResource("jar2.jar");

		assertNotNull(jar1jar);
		assertNotNull(jar2jar);
				
		final JarProcessor[] jars = new JarProcessor[] {
				new JarProcessor(new File(jar1jar.toURI())).setRelocate(true),
				new JarProcessor(new File(jar2jar.toURI())).setRelocate(true)
		};

		final FileOutputStream out = new FileOutputStream("out2.jar");

		final DuplicateHandler handler = new DuplicateHandler() {
			public JarProcessor handleDuplicate(String pName, JarProcessor[] jars) {
				return jars[0];
			}			
		};
		
		boolean result = JarUtils.processJars(jars, handler, out, new Console() {
			public void println(String pString) {
				System.out.println(pString);
			}			
		});
		
		assertFalse(result);
		
	}

}
