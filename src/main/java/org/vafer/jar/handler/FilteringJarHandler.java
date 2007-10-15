package org.vafer.jar.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

public class FilteringJarHandler extends DelegatingJarHandler {

	public interface JarEntryFilter {
		boolean accept( JarEntry entry );
	}
	
	private final JarEntryFilter filter;
	private final OutputStream output;
	
	public FilteringJarHandler( final JarEntryFilter pFilter ) {
		filter = pFilter;
		output = new NullOutputStream();
	}

	public void onResource( final JarEntry entry, final InputStream input ) throws IOException {
		
		if (filter == null) {
			super.onResource(entry, input);
			return;			
		}

		if (filter.accept(entry)) {
			super.onResource(entry, input);
			return;
		}

		IOUtils.copy(input, output);
	}
	
}
