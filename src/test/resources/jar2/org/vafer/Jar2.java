package org.vafer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Jar2 {

	public static void main(String[] args) throws Exception {
		final Class clazz = Jar2.class;
		final String resourceName = "org/vafer/some.properties";
		System.out.println(clazz + " called with " + args.length + " args");
		final InputStream is = clazz.getClassLoader().getResourceAsStream(resourceName);
		if (is != null) {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			final String content = reader.readLine();
			System.out.println("Content of " + resourceName + " is [" + content + "]");
			
		} else {
			System.out.println("Could not find resource " + resourceName);
		}
	}

}
