package org.vafer.dependency.resources.buildtime;


import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;
import org.vafer.dependency.resources.ResourceResolver;

public class BuildtimeResourceResolver implements ResourceResolver {

	public ClassAdapter getClassAdapter( final ClassVisitor adapter, final Remapper mapper) {
		return new BuildtimeResourceResolvingClassAdapter(adapter, mapper);
	}

}
