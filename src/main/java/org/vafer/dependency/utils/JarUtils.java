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
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.vafer.dependency.Console;
import org.vafer.dependency.MapperDump;
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.asm.RuntimeWrappingClassAdapter;


public final class JarUtils {

	private static class MappingEntry {
		private final String name;
		private final byte[] digestBytes;
		private JarProcessor[] versions;
		
		public MappingEntry( final JarProcessor pJar, final String pName, final byte[] pDigestBytes ) {
			name = pName;
			digestBytes = pDigestBytes;
			versions = new JarProcessor[] { pJar };
		}
		
		public void addVersion( final JarProcessor pJar ) {
			JarProcessor[] newVersions = new JarProcessor[versions.length+1];
			System.arraycopy(versions, 0, newVersions, 1, versions.length);
			newVersions[0] = pJar;
			versions = newVersions;
		}
		
		public JarProcessor[] getVersions() {
			return versions;
		}
	}
	
	public interface DuplicateHandler {
		JarProcessor handleDuplicate( final String pName, final JarProcessor[] jars );
	};
	
	private static boolean bytesAreEqual( byte[] a, byte[] b ) {
		if (a == b) {
			return true;
		}
		
		if (a.length != b.length) {
			return false;
		}
		
		for (int i = 0; i < a.length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean processJars( final JarProcessor[] pJars, final DuplicateHandler pDuplicateHandler, final FileOutputStream pOutput, final Console pConsole ) throws IOException {
        boolean changed = false;

        final String mapperName = "org/vafer/Mapper.class";

        final Map mapping = new HashMap(pJars.length*50);
        final MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("md5 algorithm missing to process jars " + e);
		}
        
        for (int i = 0; i < pJars.length; i++) {
        	final JarProcessor jar = pJars[i];

        	final JarInputStream inputStream = jar.getInputStream();

            while (true) {
                final JarEntry entry = inputStream.getNextJarEntry();
                
                if (entry == null) {
                    break;
                }

                digest.reset();
                final DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);                
                IOUtils.copy(digestInputStream, new NullOutputStream());
                final byte[] digestBytes = digest.digest();
                
                final String oldName = entry.getName();

                if (jar.keepResourceWithName(oldName)) {
                	
                	final String newName = jar.getNewNameFor(oldName);
                	
                	final MappingEntry mappingEntry = new MappingEntry(jar, newName, digestBytes); 
                	
                	if (mapping.containsKey(oldName)) {
                		final MappingEntry previousEntry = (MappingEntry) mapping.get(oldName);
                		
                		if (bytesAreEqual(digestBytes, previousEntry.digestBytes)) {
                			// we have the same class twice in classpath
                			
                			if(pConsole != null) {
                				pConsole.println("ignoring duplicate resource " + oldName);
                			}
                			
                			continue;
                		}
                		
                		// classpath clash!!

                		if(pConsole != null) {
            				pConsole.println("found duplicate resource path " + oldName);
            			}
            			
                		// register the multiple version
            			mappingEntry.addVersion(jar);
                	}
                	
                	mapping.put(oldName, mappingEntry);
                }    

                mapping.put(mapperName, new MappingEntry(jar, jar.getNewNameFor(mapperName), null));
            }
        }        
                
        final JarOutputStream outputJar = new JarOutputStream(pOutput);

        if(pConsole != null) {
			pConsole.println("building new jar");
		}

        final ResourceRenamer renamer = new ResourceRenamer() {
			public String getNewNameFor(String pOldName) {
				
				final MappingEntry mappingEntry = (MappingEntry) mapping.get(pOldName);

				if (mappingEntry == null) {
					return pOldName;
				}

				return mappingEntry.name;
			}        	
        };
        
        
        
        for (int i = 0; i < pJars.length; i++) {
        	final JarProcessor jar = pJars[i];

        	final Map localMapping = new HashMap();
        	final String localMapperName = jar.getNewNameFor(mapperName);

            final JarInputStream inputStream = jar.getInputStream();
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
                final String newName = renamer.getNewNameFor(oldName);

                if (!jar.keepResourceWithName(oldName)) {

                	if (pConsole != null) {
                        pConsole.println("removing resource " + oldName);
                    }

                    changed = true;
                    
                    IOUtils.copy(inputStream, new NullOutputStream());
                    continue;
                }
                
                final MappingEntry m = (MappingEntry) mapping.get(oldName);
                final JarProcessor[] versions = m.getVersions();

                if (versions.length > 0) {
                	if (pDuplicateHandler != null) {
                		final JarProcessor finalVersion = pDuplicateHandler.handleDuplicate(oldName, versions);
                		if (finalVersion != jar) {

                			if (pConsole != null) {
                                pConsole.println("ignoring  duplicate resource " + oldName + " from " + jar);
                            }

                        	continue;
                		}
                	} else {
                		throw new IOException("duplicate found an could not be resolved");
                	}
                	
                }
                
                if (newName.equals(oldName)) {

                	if (pConsole != null) {
                        pConsole.println("keeping original resource " + oldName);
                    }

                    outputJar.putNextEntry(new JarEntry(oldName));
                    
                    IOUtils.copy(inputStream, outputJar);
                    continue;
                }

                if (pConsole != null) {
                    pConsole.println("renaming resource " + oldName + "->" + newName);
                }
                
                changed = true;

                localMapping.put(oldName, newName);
                                
                outputJar.putNextEntry(new JarEntry(newName));

                if (oldName.endsWith(".class")) {
                    final byte[] oldClassBytes = IOUtils.toByteArray(inputStream);

                    if (pConsole != null) {
                        pConsole.println("adjusting class " + oldName + "->" + newName);
                    }
                    
                    final ClassReader r = new ClassReader(oldClassBytes);
                    final ClassWriter w = new ClassWriter(true);
                    r.accept(new RenamingVisitor(new RuntimeWrappingClassAdapter(w, localMapperName, pConsole), renamer), false);

                    final byte[] newClassBytes = w.toByteArray();
                    IOUtils.copy(new ByteArrayInputStream(newClassBytes), outputJar);
                } else {
                    IOUtils.copy(inputStream, outputJar);                                                
                }
            }
            
            if (localMapping.size() > 0) {
                outputJar.putNextEntry(new JarEntry(localMapperName));
                try {
					final byte[] clazzBytes = MapperDump.dump(localMapperName, localMapping);
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
