package org.vafer.jar.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;

import org.vafer.jar.Jar;

public class DelegatingJarHandler implements JarHandler {

	private JarHandler delegate;
	
	public DelegatingJarHandler chain( JarHandler pDelegate ) {
		delegate = pDelegate;
		return this;
	}

	public void onStartProcessing() throws IOException {
		delegate.onStartProcessing();
	}

	public void onStartJar(Jar jar) throws IOException {
		delegate.onStartJar(jar);
	}

	public void onResource(JarEntry entry, InputStream input) throws IOException {
		delegate.onResource(entry, input);
	}

	public void onStopJar(Jar jar) throws IOException {
		delegate.onStopJar(jar);
	}

	public void onStopProcessing() throws IOException {
		delegate.onStopProcessing();
	}

}
