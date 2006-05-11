package org.vafer.dependency.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.objectweb.asm.ClassReader;
import org.vafer.dependency.asm.DependencyVisitor;

public final class DependencyUtils {

	public static Set getDependenciesOfJar( final InputStream pInputStream ) throws IOException {
        final JarInputStream inputStream = new JarInputStream(pInputStream);
        final NullOutputStream nullStream = new NullOutputStream();
        final Set dependencies = new HashSet();
        try {
	        while (true) {
	            final JarEntry entry = inputStream.getNextJarEntry();
	            
	            if (entry == null) {
	                break;
	            }
	            
	            if (entry.isDirectory()) {
	                // ignore directory entries
	                IOUtils.copy(inputStream, nullStream);
	                continue;
	            }
	            
	            final String name = entry.getName();
	            
	            if (name.endsWith(".class")) {
	                final DependencyVisitor v = new DependencyVisitor();
	                new ClassReader( inputStream ).accept( v, false );
	                dependencies.addAll(v.getDependencies());
	            } else {
	                IOUtils.copy(inputStream, nullStream);                                                
	            }
	        }
        } finally {
        	inputStream.close();
        }
        
        return dependencies;
	}

	public static Set getDependenciesOfClass( final InputStream pInputStream ) throws IOException {
        final DependencyVisitor v = new DependencyVisitor();
        new ClassReader( pInputStream ).accept( v, false );
        final Set depNames = v.getDependencies();
        return depNames;
	}
	
	public static Set getDependenciesOfClass( final Class pClass ) throws IOException {
		final String resource = "/" + pClass.getName().replace('.', '/') + ".class";		
		return getDependenciesOfClass(pClass.getResourceAsStream(resource));
	}

}
