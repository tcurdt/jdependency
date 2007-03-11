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
package org.vafer.dependency.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.vafer.dependency.Console;
import org.vafer.dependency.MapperDump;
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.asm.RuntimeWrappingClassAdapter;


public final class JarUtils {
    	
	public static boolean combineJars(
            final File[] pInputJars,
            final ResourceMatcher[] pMatchers,
            final ResourceRenamer[] pRenamers,
            final File pOutputJar
            ) throws IOException {
		return combineJars(pInputJars, pMatchers, pRenamers, pOutputJar, null);
	}

	public static boolean combineJars(
			final File[] pInputJars,
			final ResourceMatcher[] pMatchers,
			final ResourceRenamer[] pRenamers,
			final File pOutputJar,
			final Console pConsole
			) throws IOException {
        
        boolean changed = false;
        
        final JarInputStream[] inputStreams = new JarInputStream[pInputJars.length];
        for (int i = 0; i < inputStreams.length; i++) {
            inputStreams[i] = new JarInputStream(new FileInputStream(pInputJars[i]));
        }
        
        final JarOutputStream outputJar = new JarOutputStream(new FileOutputStream(pOutputJar));
                
        for (int i = 0; i < inputStreams.length; i++) {
        	final Map resourceMap = new HashMap();
            final JarInputStream inputStream = inputStreams[i];
        	final String mapper = pRenamers[i].getNewNameFor("org/vafer/Mapper");
            while (true) {
                final JarEntry entry = inputStream.getNextJarEntry();
                
                if (entry == null) {
                    break;
                }
                
                if (entry.isDirectory()) {
                    // ignore directory entries
                    if (pConsole != null) {
                        pConsole.println("removing directory entry " + entry);
                    }
                    IOUtils.copy(inputStream, new NullOutputStream());
                    continue;
                }
                
                final String oldName = entry.getName();
                
                if (!pMatchers[i].keepResourceWithName(oldName)) {
                    changed = true;
                    if (pConsole != null) {
                        pConsole.println("removing resource " + oldName);
                    }

                    IOUtils.copy(inputStream, new NullOutputStream());
                    continue;
                }

                final String newName = pRenamers[i].getNewNameFor(oldName);
                
                if (newName.equals(oldName)) {
                    if (pConsole != null) {
                        pConsole.println("keeping original resource " + oldName);
                    }

                    outputJar.putNextEntry(new JarEntry(newName));
                    IOUtils.copy(inputStream, outputJar);
                    continue;
                }

                if (pConsole != null) {
                    pConsole.println("renaming resource " + oldName + "->" + newName);
                }
                
                resourceMap.put(oldName, newName);
                                
                changed = true;

                try{ 
                	outputJar.putNextEntry(new JarEntry(newName));
                } catch(ZipException e) {
                	if (e.getMessage().startsWith("duplicate entry:")) {
                		
                		pConsole.println("duplicate entry " + newName);
                		
	                	IOUtils.copy(inputStream, new NullOutputStream());
	                	continue;
                	}
                }

                if (oldName.endsWith(".class")) {
                    final byte[] oldClassBytes = IOUtils.toByteArray(inputStream);

                    if (pConsole != null) {
                        pConsole.println("adjusting class " + oldName + "->" + newName);
                    }
                    
                    final ClassReader r = new ClassReader(oldClassBytes);
                    final ClassWriter w = new ClassWriter(true);
                    r.accept(new RenamingVisitor(new RuntimeWrappingClassAdapter(w, mapper, pConsole), pRenamers[i]), false);

                    final byte[] newClassBytes = w.toByteArray();
                    IOUtils.copy(new ByteArrayInputStream(newClassBytes), outputJar);
                } else {
                    IOUtils.copy(inputStream, outputJar);                                                
                }
            }
            
            // add a class for runtime resolving of renamed resources
            if (pConsole != null) {
            	for (Iterator it = resourceMap.entrySet().iterator(); it.hasNext();) {
					final Map.Entry mapping = (Map.Entry) it.next();
					
					final String oldName = (String) mapping.getKey();
					final String newName = (String) mapping.getValue();
					
	                pConsole.println("mapped " + oldName + "->" + newName);					
				}            	
            }
            
            if (resourceMap.size() > 0) {
                outputJar.putNextEntry(new JarEntry(mapper + ".class"));
                try {
					final byte[] clazzBytes = MapperDump.dump(mapper, resourceMap);
                    IOUtils.copy(new ByteArrayInputStream(clazzBytes), outputJar);					
				} catch (Exception e) {
					throw new IOException("could not generate mapper class " + e);
				}            	
            }            
            
            inputStream.close();
        }
        outputJar.close();
        
        return changed;
    }

}
