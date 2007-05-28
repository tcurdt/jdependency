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
package org.vafer.dependency.relocation;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
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


public final class JarProcessor {

	private final static String mapperName = "org/vafer/RuntimeMapper.class";

	private static final class MappingEntry {
		private final String oldName;
		private final String newName;
		private Version[] versions;
		
		public MappingEntry( final String pOldName, final String pNewName, final Version pVersion ) {
			oldName = pOldName;
			newName = pNewName;
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
		
		public String getOldName() {
			return oldName;
		}

		public String getNewName() {
			return newName;
		}
		
		public String toString() {
			return oldName;
		}
	}

	private static byte[] calculateDigest( final MessageDigest digest, final InputStream inputStream ) throws IOException {
        digest.reset();
        final DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);                
        IOUtils.copy(digestInputStream, new NullOutputStream());
        return digest.digest();
	}
	
	
	private static Map getMapping( final Jar[] pJars, final Console pConsole ) throws IOException, NoSuchAlgorithmException {
        
		final MessageDigest digest = MessageDigest.getInstance("MD5");
        
        final Map byOldName = new HashMap(pJars.length*50);
        
        for (int i = 0; i < pJars.length; i++) {
        	final Jar jar = pJars[i];

        	final JarInputStream inputStream = jar.getInputStream();

            while (true) {
                final JarEntry entry = inputStream.getNextJarEntry();
                
                if (entry == null) {
                    break;
                }

                final String oldName = entry.getName();                
                final String newName = jar.getPrefix() + oldName;
                
                final byte[] digestBytes = calculateDigest(digest, inputStream);                

        		final MappingEntry mappingEntry = (MappingEntry) byOldName.get(newName);
        		if (mappingEntry == null) {
        			// a new resource
            		byOldName.put(oldName, new MappingEntry(oldName, newName, new Version(jar, digestBytes)));
        		} else {
        			// a new version of resource seen before
        			mappingEntry.addVersion(new Version(jar, digestBytes));
        		}
            }
            
            IOUtils.closeQuietly(inputStream);
        }        
		
        return byOldName;
	}
	
	
	public static void processJars( final Jar[] pJars, final ResourceHandler pHandler, final FileOutputStream pOutput, final Console pConsole ) throws IOException, NoSuchAlgorithmException {

        final Map byOldName = getMapping(pJars, pConsole);
                
        final JarOutputStream outputStream = new JarOutputStream(pOutput);

        if(pConsole != null) {
			pConsole.println("Building new jar with mappings:");
			
			for (Iterator it = byOldName.values().iterator(); it.hasNext();) {
				final MappingEntry mappingEntry = (MappingEntry) it.next();
				
				pConsole.println(" " + mappingEntry.getOldName() + " -> " + mappingEntry.getNewName() + " [" + mappingEntry.versions.length + "]");				
			}
		}

        pHandler.onStartProcessing(outputStream);
        
        final ResourceRenamer renamer = new ResourceRenamer() {
			public String getNewNameFor(String pResourceName) {
				
				final MappingEntry mappingEntry = (MappingEntry) byOldName.get(pResourceName);

				if (mappingEntry == null) {
					return pResourceName;
				}

				return mappingEntry.newName;
			}        	
        };

        for (int i = 0; i < pJars.length; i++) {
        	final Jar jar = pJars[i];

        	pHandler.onStartJar(jar, outputStream);
        	
            final JarInputStream inputStream = jar.getInputStream();

            final String localMapperName = jar.getPrefix() + mapperName;
        	
            while (true) {
                final JarEntry entry = inputStream.getNextJarEntry();
                
                if (entry == null) {
                    break;
                }
                
                if (entry.isDirectory()) {
                    // ignore directory entries
                	IOUtils.copy(inputStream, new NullOutputStream());
                    continue;
                }
                
                final String oldName = entry.getName();
                final String newName = jar.getPrefix() + oldName;
            	

                final MappingEntry m = (MappingEntry) byOldName.get(oldName);
                
                final InputStream newInputStream = pHandler.onResource(jar, oldName, newName, m.getVersions(), inputStream);
                
                if (newInputStream == null) {
                	// remove the resource
                	IOUtils.copy(inputStream, new NullOutputStream());
                	continue;
                }

                outputStream.putNextEntry(new JarEntry(newName));                    

                if (newName.endsWith(".class")) {
                    final byte[] oldClassBytes = IOUtils.toByteArray(newInputStream);

                    final ClassReader r = new ClassReader(oldClassBytes);
                    final ClassWriter w = new ClassWriter(true);
                    r.accept(new RenamingVisitor(new RuntimeWrappingClassAdapter(w, localMapperName, pConsole), renamer), false);

                    final byte[] newClassBytes = w.toByteArray();
                    IOUtils.copy(new ByteArrayInputStream(newClassBytes), outputStream);
                    continue;
                }

                IOUtils.copy(newInputStream, outputStream);                                                
            }

        	final String prefix = jar.getPrefix();
        	if ("".equals(prefix)) {
        		// not relocated
        		continue;
        	}
        
        	
        	if (pConsole != null) {
                pConsole.println("Creating runtime mapper " + localMapperName + " handling prefix " + prefix);
            }
            	
            outputStream.putNextEntry(new JarEntry(localMapperName));
            try {
				final byte[] clazzBytes = MapperDump.dump(localMapperName, jar.getPrefix());
                IOUtils.copy(new ByteArrayInputStream(clazzBytes), outputStream);					
			} catch (Exception e) {
				throw new IOException("Could not generate mapper class " + e);
			}            	
            
            IOUtils.closeQuietly(inputStream);

        	pHandler.onStopJar(jar, outputStream);

        }

        pHandler.onStopProcessing(outputStream);
        
        IOUtils.closeQuietly(outputStream);
	}
	
}
