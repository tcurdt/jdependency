package org.vafer.dependency.resources;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.commons.Remapper;

public interface ResourceResolver {

	public ClassAdapter getClassAdapter( final ClassVisitor adapter, final Remapper mapper );

}
