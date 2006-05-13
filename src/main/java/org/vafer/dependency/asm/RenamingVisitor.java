package org.vafer.dependency.asm;

import org.objectweb.asm.ClassVisitor;
import org.vafer.dependency.utils.ResourceRenamer;

public final class RenamingVisitor extends DependencyVisitor {

	private final ResourceRenamer renamer;
	
	public RenamingVisitor(final ClassVisitor pClassVisitor, final ResourceRenamer pRenamer) {
		super();
		renamer = pRenamer;
	}
	
	protected String visitDependency( final String pClassName ) {
		final String newClassName = renamer.getNewResourceNameForResource(pClassName);
		System.out.println("renaming " + pClassName + " into " + newClassName);
		return newClassName;
	}

}
