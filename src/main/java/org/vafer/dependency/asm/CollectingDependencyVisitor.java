package org.vafer.dependency.asm;

import java.util.HashSet;
import java.util.Set;

public final class CollectingDependencyVisitor extends AbstractDependencyVisitor {

	final Set classes = new HashSet();

	public Set getDependencies() {
		return classes;
	}

	protected String visitDependency( final String pClassName ) {
		classes.add(pClassName);
		return pClassName;
	}
}
