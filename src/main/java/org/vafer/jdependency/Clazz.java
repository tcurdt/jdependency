/*
 * Copyright 2010 The Apache Software Foundation.
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
package org.vafer.jdependency;

import java.util.HashSet;
import java.util.Set;

public final class Clazz implements Comparable {

	private final Set<Clazz> dependencies = new HashSet();
	private final Set<Clazz> references = new HashSet();
	private final Set<ClazzpathUnit> units = new HashSet();

	private final String name;	

	public Clazz( final String pName ) {
		name = pName;
	}

	public String getName() {
		return name;
	}
	
	
	public void addClazzpathUnit( final ClazzpathUnit pUnit ) {
		units.add(pUnit);
	}

	public void removeClazzpathUnit( final ClazzpathUnit pUnit ) {
		units.remove(pUnit);
	}
	
	public Set getClazzpathUnits() {
		return units;
	}


	public void addDependency( final Clazz pClazz ) {
		pClazz.references.add(this);
		dependencies.add(pClazz);
	}

	public void removeDependency( final Clazz pClazz ) {
		pClazz.references.remove(this);
		dependencies.remove(pClazz);		
	}
	
	public Set getDependencies() {
		return dependencies;
	}


	
	public Set getReferences() {
		return references;
	}

	
	public Set getTransitiveDependencies() {
		final Set all = new HashSet();
		findTransitiveDependencies(all);
		return all;
	}

	void findTransitiveDependencies( final Set pAll ) {

		for (Clazz clazz : dependencies) {
			if (!pAll.contains(clazz)) {
				pAll.add(clazz);
				clazz.findTransitiveDependencies(pAll);
			}
		}
	}

	public boolean equals( final Object pO ) {
		if (pO.getClass() != Clazz.class) {
			return false;
		}
		final Clazz c = (Clazz) pO;
		return name.equals(c.name);
	}

	public int hashCode() {
		return name.hashCode();
	}

	public int compareTo( final Object pO ) {
		return name.compareTo(((Clazz) pO).name);
	}

	public String toString() {
		return name;
	}

}
