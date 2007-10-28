package org.vafer.dependency.resources;

import java.util.HashSet;
import java.util.Set;

public final class ResolverUtils {

	// static java.lang.Class       java.lang.Class.forName(java.lang.String)
	// static java.lang.Class       java.lang.Class.forName(java.lang.String, boolean, java.lang.ClassLoader)
	// static java.net.URL          java.lang.ClassLoader.getSystemResource(java.lang.String)
	// static java.io.InputStream   java.lang.ClassLoader.getSystemResourceAsStream(java.lang.String)
	// static java.util.Enumeration java.lang.ClassLoader.getSystemResources(java.lang.String)
	// java.lang.Class              java.lang.ClassLoader.loadClass(java.lang.String)
	// java.net.URL                 java.lang.ClassLoader.getResource(java.lang.String)
	// java.io.InputStream          java.lang.ClassLoader.getResourceAsStream(java.lang.String)
	
	private final static Set resolveClassMethodsClass = new HashSet() {
		private static final long serialVersionUID = 1L;
		{
			add("forName");
		}
	};
	private final static Set resolveClassMethodsClassLoader = new HashSet() {
		private static final long serialVersionUID = 1L;
		{
			add("loadClass");
		}
	};

	private final static Set resolveResouceMethodsClass = new HashSet() {
		private static final long serialVersionUID = 1L;
		{
			add("getResourceAsStream");
			add("getResource");
		}
	};

	private final static Set resolveResourceMethodsClassLoader = new HashSet() {
		private static final long serialVersionUID = 1L;
		{
			add("getSystemResource");
			add("getSystemResourceAsStream");
			add("getResource");
			add("getSystemResources");
			add("getResourceAsStream");
		}
	};

	
	public static boolean needsResourceResolving( String owner, String name ) {
		if (("java/lang/Class".equals(owner) && resolveResouceMethodsClass.contains(name)) ||
			("java/lang/ClassLoader".equals(owner) && resolveResourceMethodsClassLoader.contains(name))) {
			return true;
		}
		return false;
	}

	public static boolean needsClassResolving( String owner, String name ) {
		if (("java/lang/Class".equals(owner) && resolveClassMethodsClass.contains(name)) ||
		    ("java/lang/ClassLoader".equals(owner) && resolveClassMethodsClassLoader.contains(name))) {
			return true;
		}
		return false;
	}

}
