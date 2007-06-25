package org.vafer.dependency.runtime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassAdapter;
import org.vafer.dependency.Console;
import org.vafer.dependency.asm.RuntimeClassAdapter;

public final class RegistryRuntime implements MapperRuntime {
	
	private final Console console;
	private final Map mapping;
	private final String runtimeClass;
	private final boolean isRequired;
	
	public RegistryRuntime( final Map pMapping, final Console pConsole ) {
		mapping = pMapping;
		console = pConsole;
		runtimeClass = "org/vafer/dependency/RuntimeMapper";
		
		boolean same = true;
		for (Iterator it = mapping.entrySet().iterator(); it.hasNext();) {
			final Map.Entry entry = (Map.Entry) it.next();
			final String key = (String) entry.getKey();
			final String value = (String) entry.getValue();

			if (!key.equals(value)) {
				same = false;
				break;
			}
		}
		isRequired = !same;
	}
	
	public ClassAdapter getClassAdapter( final ClassAdapter pAdapter ) {
		if (!isRequired) {
			return pAdapter;
		}
		return new RuntimeClassAdapter(pAdapter, runtimeClass);
	}
	
	public void addRuntime( final JarOutputStream pOutput ) throws IOException {
		if (!isRequired) {
	        console.println("No runtime mapper required");
			return;
		}
		
        console.println("Creating runtime mapper " + runtimeClass);
    	
        pOutput.putNextEntry(new JarEntry(runtimeClass + ".class"));
        try {
			final byte[] clazzBytes = MapperDump.dump(runtimeClass, mapping);
            IOUtils.copy(new ByteArrayInputStream(clazzBytes), pOutput);					
		} catch (Exception e) {
			throw new IOException("Could not generate mapper class " + e);
		}

	}

}
