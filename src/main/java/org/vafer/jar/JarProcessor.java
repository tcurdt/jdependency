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
package org.vafer.jar;

import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.vafer.jar.handler.JarHandler;


public final class JarProcessor {

	public void processJars( final Jar[] pJars, final JarHandler pHandler ) throws IOException {

        pHandler.onStartProcessing();        
        
        for (int i = 0; i < pJars.length; i++) {
        	final Jar jar = pJars[i];
        	
        	pHandler.onStartJar(jar);
        	
            final JarInputStream inputStream = jar.getInputStream();

            while (true) {
                final JarEntry entry = inputStream.getNextJarEntry();
                
                if (entry == null) {
                    break;
                }
                
                // TODO: really?
                if (entry.isDirectory()) {
                    // ignore directory entries
                	IOUtils.copy(inputStream, new NullOutputStream());
                    continue;
                }

                pHandler.onResource(entry, inputStream);                
            }

        	pHandler.onStopJar(jar);
        }

        pHandler.onStopProcessing();
	}
}
