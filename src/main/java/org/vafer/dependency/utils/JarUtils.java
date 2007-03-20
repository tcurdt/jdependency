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
import org.vafer.dependency.asm.RenamingVisitor;
import org.vafer.dependency.asm.RuntimeWrappingClassAdapter;
import org.vafer.dependency.resources.MapperDump;
import org.vafer.dependency.resources.ResourceRenamer;
import org.vafer.dependency.resources.Version;


public final class JarUtils {

	private final static String mapperName = "org/vafer/RuntimeMapper.class";

	private static final class MappingEntry {
		private final String name;
		private Version[] versions;
		
		public MappingEntry( final String pName, final Version pVersion ) {
			name = pName;						
			versions = new Version[] { pVersion };
		}
		
		public void addVersion( final Version pVersion ) {
			final Version[] newVersions = new Version[versions.length+1];
			System.arraycopy(versions, 0, newVersions, 1, versions.length);
			newVersions[0] = pVersion;
			versions = newVersions;
		}
		
		public Version[] getVersions() {
			return versions;
		}
	}
	
	private static Map getMapping( final Jar[] pJars, final Console pConsole ) throws IOException, NoSuchAlgorithmException {
        
		final MessageDigest digest = MessageDigest.getInstance("MD5");
        
        final Map mapping = new HashMap(pJars.length*50);
        
        for (int i = 0; i < pJars.length; i++) {
        	final Jar jar = pJars[i];

        	final JarInputStream inputStream = jar.getInputStream();

            while (true) {
                final JarEntry entry = inputStream.getNextJarEntry();
                
                if (entry == null) {
                    break;
                }

                final String oldName = entry.getName();
                
                if (!jar.keepResource(oldName)) {
                	if(pConsole != null) {
        				pConsole.println("removing resource " + oldName);
        			}                	
                	continue;
                }

                final String newName = jar.getNewNameFor(oldName);

                if(pConsole != null) {
    				pConsole.println("keeping resource " + oldName + " as " + newName);
    			}                	
                
                digest.reset();
                final DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);                
                IOUtils.copy(digestInputStream, new NullOutputStream());
                final byte[] digestBytes = digest.digest();
                

        		final MappingEntry mappingEntry = (MappingEntry) mapping.get(oldName);
        		if (mappingEntry == null) {
                	mapping.put(oldName, new MappingEntry(newName, new Version(jar, digestBytes)));
        		} else {
        			mappingEntry.addVersion(new Version(jar, digestBytes));
        		}
            }            
            
        }        
		
        return mapping;
	}
	
	
	public static void processJars( final Jar[] pJars, final FileOutputStream pOutput, final Console pConsole ) throws IOException, NoSuchAlgorithmException {

        final Map mapping = getMapping(pJars, pConsole);
                
        final JarOutputStream outputStream = new JarOutputStream(pOutput);

        if(pConsole != null) {
			pConsole.println("building new jar with " + mapping.size() + " resources");
		}

        final ResourceRenamer renamer = new ResourceRenamer() {
			public String getNewNameFor(String pResourceName) {
				
				final MappingEntry mappingEntry = (MappingEntry) mapping.get(pResourceName);

				if (mappingEntry == null) {
					return pResourceName;
				}

				return mappingEntry.name;
			}        	
        };
        
        
        
        for (int i = 0; i < pJars.length; i++) {
        	final Jar jar = pJars[i];

        	jar.onStart(outputStream);
        	
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
                final String newName = jar.getNewNameFor(oldName);

                if (!jar.keepResource(oldName)) {

                	if (pConsole != null) {
                        pConsole.println("removing resource " + oldName);
                    }

                    IOUtils.copy(inputStream, new NullOutputStream());
                    continue;
                }
                
                final MappingEntry m = (MappingEntry) mapping.get(oldName);
                final Version[] versions = m.getVersions();
                
                if (versions.length > 1) {
                	final Version finalVersion = jar.pickVersion(versions);
                	
            		if (finalVersion.getJar() != jar) {

            			if (pConsole != null) {
                            pConsole.println("ignoring duplicate resource " + oldName + " from " + jar);
                        }

                    	continue;
            		} else {

            			if (pConsole != null) {
                            pConsole.println("using duplicate resource " + oldName + " from " + jar);
                        }
            			
            		}
                }
                
                if (newName.equals(oldName)) {

                	if (pConsole != null) {
                        pConsole.println("keeping original resource " + oldName);
                    }

                    outputStream.putNextEntry(new JarEntry(oldName));
                    
                    IOUtils.copy(inputStream, outputStream);
                    continue;
                }

                if (pConsole != null) {
                    pConsole.println("renaming resource " + oldName + " -> " + newName);
                }
                
                localMapping.put(oldName, newName);
                                
                outputStream.putNextEntry(new JarEntry(newName));

                if (oldName.endsWith(".class")) {
                    final byte[] oldClassBytes = IOUtils.toByteArray(inputStream);

                    final ClassReader r = new ClassReader(oldClassBytes);
                    final ClassWriter w = new ClassWriter(true);
                    r.accept(new RenamingVisitor(new RuntimeWrappingClassAdapter(w, localMapperName, pConsole), renamer), false);

                    final byte[] newClassBytes = w.toByteArray();
                    IOUtils.copy(new ByteArrayInputStream(newClassBytes), outputStream);
                } else {
                    IOUtils.copy(inputStream, outputStream);                                                
                }
            }
            
            if (localMapping.size() > 0) {
                
            	if (pConsole != null) {
                    pConsole.println("creating runtime mapper " + localMapperName + " handling " + localMapping.size() + " resources");
                }
            	
            	outputStream.putNextEntry(new JarEntry(localMapperName));
                try {
					final byte[] clazzBytes = MapperDump.dump(localMapperName, localMapping);
                    IOUtils.copy(new ByteArrayInputStream(clazzBytes), outputStream);					
				} catch (Exception e) {
					throw new IOException("could not generate mapper class " + e);
				}            	
            }            
            
            IOUtils.closeQuietly(inputStream);

        	jar.onStop(outputStream);

        }
        
        IOUtils.closeQuietly(outputStream);
	}
	
}
