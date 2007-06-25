package org.vafer.dependency.runtime;

import java.util.HashMap;

public final class Mapper {

	private final static boolean debug = "true".equalsIgnoreCase(System.getProperty("org.vafer.dependency.relocation.debug"));
	private final static String clazzName = Mapper.class.getName();

	private final static HashMap mapping;

	static {
		mapping = new HashMap();
		mapping.put("old", "new");
	}

	public static String resolveResource( final String oldName ) {
		String newName = (String) mapping.get(oldName);

		if (newName == null) {
			newName = oldName;
		}

		if (debug) {
			System.out.println("*** " + clazzName + " mapping resource request " + oldName + " -> " + newName);
		}
		
		return newName;
	}

	public static String resolveClass( final String oldName ) {		
		final String oldResourceName = oldName.replace('.', '/') + ".class";
		final String newResourceName = resolveResource(oldResourceName);
		final String newName = newResourceName.substring(0, newResourceName.length() - 6).replace('/', '.');
		return newName;
	}
}
