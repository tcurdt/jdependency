package org.vafer.jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;

import junit.framework.TestCase;

public abstract class AbstractJarTestCase extends TestCase {

	public static String classToResource( final String name ) {
		final String resource = name.replace('.', '/') + ".class";
		return resource;
	}


	public static File createJar( Class pClazz, final String[] pResources ) throws IOException {

		final File temp = File.createTempFile("dep", ".jar");
		temp.deleteOnExit();

		final JarOutputStream output = new JarOutputStream(new FileOutputStream(temp));

		for (int i = 0; i < pResources.length; i++) {
			final JarEntry entry = new JarEntry(pResources[i]);

			final InputStream data = pClazz.getClassLoader().getResourceAsStream(pResources[i]);

			if (data == null) {
				throw new IOException("Could not find resource " + pResources[i]);
			}

			output.putNextEntry(entry);

			IOUtils.copy(data, output);

			data.close();
		}

		output.close();

		return temp;
	}

}
