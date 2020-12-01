package org.vafer.jdependency.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.vafer.jdependency.Clazz;
import org.vafer.jdependency.ClazzpathUnit;

public final class ClashUtil {
	private ClashUtil() {
		throw new IllegalAccessError();
	}

	public static void removeIdenticalClasses(Set<Clazz> clashedClazzes, Map<String,Path> jarLocationsById) {
		clashedClazzes.removeIf(clazz -> compareBinary(clazz, jarLocationsById));
	}

    private static boolean compareBinary(Clazz clazz, Map<String,Path> jarLocationsById) {
    	Set<ClazzpathUnit> clazzpathUnits = clazz.getClazzpathUnits();
    	byte[][] resources = clazzpathUnits.stream()
    									   .map(clazzpathUnit -> {
						    					  String unit = clazzpathUnit.toString();
						    					  try {
						    						Path jarPath = jarLocationsById.get(clazzpathUnit.toString());	//id
													return loadClassBinary(jarPath, clazz);
						    					  } catch (IOException e) {
						    						  throw new RuntimeException("Unable to load class "+clazz+" from "+unit, e);
						    					  }
					    				   	}).toArray(byte[][]::new);
    	byte[] first = resources[0];
		for (int i = 1; i < resources.length; i++) {
			if (!Arrays.equals(first, resources[i])) {
				return false;
			}
		}
		return true;
	}

    private static byte[] loadClassBinary(Path path, Clazz clazz) throws IOException {
    	String ressourceName = clazz.getName().replace('.', '/')+".class";
    	try (JarFile jar = new JarFile(path.toFile())){
    		JarEntry entry = jar.getJarEntry(ressourceName);
    		return IOUtils.toByteArray(jar.getInputStream(entry));
    	}
    }
}
