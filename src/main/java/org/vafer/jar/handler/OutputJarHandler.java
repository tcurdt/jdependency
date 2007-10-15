package org.vafer.jar.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.vafer.jar.Jar;

public class OutputJarHandler implements JarHandler {

	private final JarOutputStream output;
	
	public OutputJarHandler( final OutputStream pOutput ) throws IOException {
		output = new JarOutputStream(pOutput);
	}
	
	public void onStartProcessing() throws IOException {
	}

	public void onStartJar(Jar jar) throws IOException {
	}

	public void onResource(JarEntry entry, InputStream input ) throws IOException {
		output.putNextEntry(new JarEntry(entry));
		IOUtils.copy(input, output);
	}
	
	public void onStopJar(Jar jar) throws IOException {
	}

	public void onStopProcessing() throws IOException {
		output.flush();
		output.close();
	}
	
}
