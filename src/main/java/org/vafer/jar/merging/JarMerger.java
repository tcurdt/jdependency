package org.vafer.jar.merging;

import java.io.IOException;
import java.io.OutputStream;

import org.vafer.jar.Jar;
import org.vafer.jar.JarProcessor;
import org.vafer.jar.handler.DelegatingJarHandler;
import org.vafer.jar.handler.FilteringJarHandler;
import org.vafer.jar.handler.JarHandler;
import org.vafer.jar.handler.OutputJarHandler;
import org.vafer.jar.handler.RenamingJarHandler;
import org.vafer.jar.handler.FilteringJarHandler.JarEntryFilter;
import org.vafer.jar.handler.RenamingJarHandler.Mapper;

public final class JarMerger {

	private final JarEntryFilter filter;
	private final Mapper mapper;

	public JarMerger() {
		filter = null;
		mapper = null;
	}
	
	public JarMerger( final JarEntryFilter pFilter, final Mapper pMapper ) {
		filter = pFilter;
		mapper = pMapper;
	}
	
	public void merge( final Jar[] jars, final DelegatingJarHandler[] customHandlers, final MergeStrategy strategy, final OutputStream output ) throws IOException {
		
		JarHandler handler;
		
		handler = new OutputJarHandler(output);		
		handler = new RenamingJarHandler(mapper).chain(handler);
		handler = strategy.getMergeHandler(jars).chain(handler);
		
		if (customHandlers != null) {
			for (int i = 0; i < customHandlers.length; i++) {
				handler = customHandlers[i].chain(handler);
			}
		}
		
		handler = new FilteringJarHandler(filter).chain(handler);

		new JarProcessor().processJars(jars, handler);
	}
}
