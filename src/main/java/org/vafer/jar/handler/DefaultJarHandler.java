package org.vafer.jar.handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.jar.JarEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.vafer.jar.Jar;

public class DefaultJarHandler implements JarHandler {

	private final OutputStream output = new NullOutputStream();
	
	public void onStartProcessing() throws IOException {
	}

	public void onStartJar(Jar jar) throws IOException {
	}

	public void onResource(JarEntry entry, InputStream input ) throws IOException {
		IOUtils.copy(input, output);
	}

	public void onStopJar(Jar jar) throws IOException {
	}

	public void onStopProcessing() throws IOException {
		output.close();
	}
	
}
