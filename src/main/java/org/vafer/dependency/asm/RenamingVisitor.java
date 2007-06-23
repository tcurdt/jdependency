/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vafer.dependency.asm;

import org.objectweb.asm.ClassVisitor;
import org.vafer.dependency.relocation.ResourceRenamer;

public final class RenamingVisitor extends DependencyVisitor {

	private final ResourceRenamer renamer;
	
	public RenamingVisitor(final ClassVisitor pClassVisitor, final ResourceRenamer pRenamer) {
		super(pClassVisitor);
		renamer = pRenamer;
	}
	
	protected String visitDependency( final String pOldName ) {
		
		final String oldResourceName = pOldName + ".class";
		final String newResourceName = renamer.getNewNameFor(oldResourceName);
		final String newName = newResourceName.substring(0, newResourceName.length() - ".class".length());		

		if (!pOldName.equals(newName)) {
			System.out.println("   " + pOldName + " => " + newName);
		}
		
		return newName;
	}

}
