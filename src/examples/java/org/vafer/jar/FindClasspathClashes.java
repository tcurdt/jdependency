package org.vafer.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;

import org.vafer.jar.handler.VersionsJarHandler;
import org.vafer.jar.handler.VersionsJarHandler.Version;

public final class FindClasspathClashes {

	public static void main(String[] args) throws Exception {
		final Jar jar = new Jar(new File(args[0]));

		final VersionsJarHandler handler = new VersionsJarHandler() {
			public void onResource(JarEntry entry, InputStream input) throws IOException {
				final String name = entry.getName();

				if (name.endsWith(".jar")) {
					new JarProcessor().processJars( new Jar[] { new Jar(input, name) }, this);
					return;
				}

				super.onResource(entry, input);
			}			
		};
				
		new JarProcessor().processJars(new Jar[] { jar }, handler);
		
		final String[] names = handler.getNames();
		for (int i = 0; i < names.length; i++) {
			final String name = names[i];
			final Version[] versions = handler.getVersions(name);
			
			if (versions.length > 1) {
				
				boolean error = false;
				final String hash = versions[0].getDigestString();
				for (int j = 0; j < versions.length; j++) {
					final Version version = versions[j];
					if (!hash.equals(version.getDigestString())) {
						error = true;
					}
				}
				
				if (error) {
					System.out.print("ERROR: ");
				} else {
					System.out.print("WARNING: ");					
				}
				
				System.out.println("Found classpath clash for " + name);

				for (int j = 0; j < versions.length; j++) {
					final Version version = versions[j];
					System.out.println(" in " + version.getJar() + " (" + version.getDigestString() + ")");
				}
			}
		}		
	}

}
