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
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.vafer.dependency.Console;


public final class JarUtils {

    
    public static boolean combineJars(
            final File[] pInputJars,
            final ResourceMatcher pMatcher,
            final ResourceRenamer pRenamer,
            final File pOutputJar
            ) throws IOException {

        Console pConsole = null;
        
        boolean changed = false;
        
        final JarInputStream[] inputStreams = new JarInputStream[pInputJars.length];
        for (int i = 0; i < inputStreams.length; i++) {
            inputStreams[i] = new JarInputStream(new FileInputStream(pInputJars[i]));
        }
        
        final JarOutputStream outputJar = new JarOutputStream(new FileOutputStream(pOutputJar));
                
        for (int i = 0; i < inputStreams.length; i++) {
            final JarInputStream inputStream = inputStreams[i];
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
                
                if (pMatcher.keepResourceWithName(oldName)) {
                    final String newName = pRenamer.getNewResourceNameFor(pInputJars[i], oldName);
                    
                    if (newName.equals(oldName)) {
                        if (pConsole != null) {
                            pConsole.println("keeping original resource " + oldName);
                        }

                        outputJar.putNextEntry(new JarEntry(newName));
                        IOUtils.copy(inputStream, outputJar);                    
                    } else {
                        if (pConsole != null) {
                            pConsole.println("renaming resource " + oldName + "->" + newName);
                        }
                        changed = true;

                        outputJar.putNextEntry(new JarEntry(newName));
                        if (oldName.endsWith(".class")) {
                            final byte[] oldClassBytes = IOUtils.toByteArray(inputStream);

                            if (pConsole != null) {
                                pConsole.println("adjusting class " + oldName + "->" + newName);
                            }

                            final byte[] newClassBytes = oldClassBytes;
                            IOUtils.copy(new ByteArrayInputStream(newClassBytes), outputJar);
                        } else {
                            IOUtils.copy(inputStream, outputJar);                                                
                        }
                    }                    


                } else {
                    changed = true;
                    if (pConsole != null) {
                        pConsole.println("removing resource " + oldName);
                    }

                    IOUtils.copy(inputStream, new NullOutputStream());
                }            
            }
            inputStream.close();
        }
        outputJar.close();
        
        return changed;
    }

}
