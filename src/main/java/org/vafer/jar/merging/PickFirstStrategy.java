package org.vafer.jar.merging;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.vafer.jar.Jar;
import org.vafer.jar.handler.DelegatingJarHandler;

public final class PickFirstStrategy implements MergeStrategy {
	
	public DelegatingJarHandler getMergeHandler(Jar[] jars) throws IOException {
		final Set seen = new HashSet();

		final DelegatingJarHandler handler = new DelegatingJarHandler() {

			private void resourceRemove( JarEntry entry, InputStream input ) throws IOException {
				IOUtils.copy(input, new NullOutputStream());						
			}

			public void onResource(JarEntry entry, InputStream input) throws IOException {
				final String name = entry.getName(); 
				
				if (seen.contains(name)) {
					resourceRemove(entry, input);
					return;
				}
				
				super.onResource(entry, input);

				seen.add(name);
			}
			
		};
		
		return handler;
	}
}
