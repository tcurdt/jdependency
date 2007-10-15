package org.vafer.jar.merging;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.vafer.jar.Jar;
import org.vafer.jar.JarProcessor;
import org.vafer.jar.handler.DelegatingJarHandler;
import org.vafer.jar.handler.VersionsJarHandler;
import org.vafer.jar.handler.VersionsJarHandler.Version;


public class MergeSameStrategy implements MergeStrategy {

	public DelegatingJarHandler getMergeHandler( final Jar[] jars ) throws IOException {

		final VersionsJarHandler checksumHandler;
		
		try {
			checksumHandler = new VersionsJarHandler();
		} catch (NoSuchAlgorithmException e) {
			return null;
		};

		new JarProcessor().processJars(jars, checksumHandler);

		final Set seen = new HashSet();

		final DelegatingJarHandler handler = 
				new DelegatingJarHandler() {

					private void resourceRemove( JarEntry entry, InputStream input ) throws IOException {
						IOUtils.copy(input, new NullOutputStream());						
					}
			
					public void onResource( JarEntry entry, InputStream input ) throws IOException {
						final String name = entry.getName();
						final Version[] versions = checksumHandler.getVersions(name);
						
						if (versions.length == 1) {
							seen.add(name);
							super.onResource(entry, input);
							return;
						}
						
						if (sameVersions(versions)) {
							if (seen.contains(name)) {
								System.out.println("WARN: merged " + versions.length + " versions of " + name + " into one");
								resourceRemove(entry, input);
								return;								
							}
							seen.add(name);
							super.onResource(entry, input);
							return;
						}
							
						System.out.println("ERROR: found clash on " + name);
		
						resourceRemove(entry, input);
					}				

					private boolean sameVersions( final Version[] versions ) {
						final Version current = versions[0];
						for (int i = 0; i < versions.length; i++) {
							final Version version = versions[i];
							if (!current.getDigestString().equals(version.getDigestString())) {
								return false;						
							}
						}
						return true;
					}			
			};
			
		return handler;
	}

}
