package org.vafer.jar.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.vafer.dependency.resources.ResourceResolver;
import org.vafer.jar.Jar;

public class RenamingJarHandler extends DelegatingJarHandler {

	public interface Mapper {
		String getNameFor( Jar jar, String name );
	}
	
	private final Mapper mapper;
	private final ResourceResolver resolver;
	private Jar jar;
	
	public RenamingJarHandler( final Mapper pMapper ) {
		this(pMapper, null);
	}

	public RenamingJarHandler( final Mapper pMapper, final ResourceResolver pResolver ) {
		mapper = pMapper;
		resolver = pResolver;
	}


	public void onStartJar(Jar pJar) throws IOException {
		super.onStartJar(pJar);
		jar = pJar;
	}

	public void onResource( final JarEntry entry, final InputStream input ) throws IOException {
		
		if (mapper == null) {
			super.onResource(entry, input);
			return;			
		}
		
		final String oldName = entry.getName();
		final String newName = mapper.getNameFor(jar, oldName);
		
		if (oldName.equals(newName)) {
			super.onResource(entry, input);
			return;
		}
		
		final JarEntry newEntry = new JarEntry(newName);
//		newEntry.setComment(entry.getComment());
//		newEntry.setCompressedSize(entry.getCompressedSize());
//		newEntry.setCrc(entry.getCrc());
//		newEntry.setExtra(entry.getExtra());
//		newEntry.setSize(entry.getSize());
//		newEntry.setTime(entry.getTime());
		
//		super.onResource(newEntry, input);
		resourceKeep(newEntry, input);
	}

	protected void resourceKeep(JarEntry entry, InputStream input) throws IOException {
		
		if (entry.getName().endsWith(".class")) {
			resourceRewrite(entry, input);
			return;
		}

		super.onResource(entry, input);
	}

	protected void resourceRewrite(JarEntry entry, InputStream input) throws IOException {

		final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        
		final Remapper remapper =
    		new Remapper() {
				public String map( final String name ) {
					return mapper.getNameFor(jar, name);
				}
			};
		
		ClassAdapter adapter = new CheckClassAdapter(writer);
		
		if (resolver != null) {
			adapter = resolver.getClassAdapter(adapter, remapper);			
		}
		
        adapter = new RemappingClassAdapter(adapter, remapper);
		
        new ClassReader(input).accept(adapter,0);

        super.onResource(entry, new ByteArrayInputStream(writer.toByteArray()));
	}
	
}
