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

import java.io.IOException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.vafer.dependency.Clazzpath.ClashHandler;

public final class WarTestCase extends TestCase {

	
	public void testWar() throws IOException {
		final URL war = this.getClass().getClassLoader().getResource("simplewar-1.0-r91.war");		

		assertNotNull(war);

		final Clazzpath clazzpath = new Clazzpath();
		
		final ClashHandler handler = new ClashHandler() {
			public void handleClash( final Clazz pClazz ) {
				//System.out.println("class " + pClazz + " already loaded from " + pClazz.getClazzpathUnit());
			}
		};
		
		final JarInputStream inputStream = new JarInputStream(war.openStream());

		while (true) {
            final JarEntry entry = inputStream.getNextJarEntry();
            
            if (entry == null) {
                break;
            }

            final String entryName = entry.getName();

            if (!entryName.endsWith(".jar")) {
            	IOUtils.copy(inputStream, new NullOutputStream());
            	continue;
            }
            
			//System.out.println("adding jar " + entryName + " to classpath");

			clazzpath.addClazzpathUnit(inputStream, entryName, handler);
        }
		
        inputStream.close();
	}
}
