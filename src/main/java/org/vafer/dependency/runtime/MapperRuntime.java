package org.vafer.dependency.runtime;

import java.io.IOException;
import java.util.jar.JarOutputStream;

import org.objectweb.asm.ClassAdapter;

public interface MapperRuntime {

	public ClassAdapter getClassAdapter( final ClassAdapter adapter );
	
	public void addRuntime( final JarOutputStream pOutput ) throws IOException;

}
