package org.vafer.dependency.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;

import org.vafer.dependency.utils.Jar;

public class DefaultResourceHandler implements ResourceHandler {

	public void onStartProcessing(JarOutputStream pOutput) throws IOException {
	}

	public void onStartJar(Jar pJar, JarOutputStream pOutput) throws IOException {
	}

	public InputStream onResource(Jar jar, String oldName, String newName, Version[] versions, InputStream inputStream) {
		return inputStream;
	}


	public void onStopJar(Jar pJar, JarOutputStream pOutput) throws IOException {
	}

	public void onStopProcessing(JarOutputStream pOutput) throws IOException {
	}

}
