package org.vafer.dependency.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ResourceHandler {

	String getNewNameFor(final String name);

	boolean keepResource(final String name);

	Version pickVersion(final Version[] versions);

	void copy(String name, InputStream is, OutputStream os) throws IOException;
}