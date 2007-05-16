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
package org.vafer.dependency;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Clazz implements Comparable {

	private final Set dependencies = new HashSet();

	private final Set references = new HashSet();

	private final String name;
	
	private final ClazzpathUnit unit;

//	public Clazz( final String pName ) {
//		unit = null;
//		name = pName;
//	}

	public Clazz( final ClazzpathUnit pUnit, final String pName ) {
		unit = pUnit;
		name = pName;
	}

	public void addDependency( final Clazz pClazz ) {
		pClazz.references.add(this);
		dependencies.add(pClazz);
	}

	public ClazzpathUnit getClazzpathUnit() {
		return unit;
	}
	
	public Set getReferences() {
		return references;
	}

	public Set getDependencies() {
		return dependencies;
	}

	public Set getTransitiveDependencies() {
		final Set all = new HashSet();
		findTransitiveDependencies(all);
		return all;
	}

	void findTransitiveDependencies(final Set pAll) {

		for (final Iterator it = dependencies.iterator(); it.hasNext();) {
			final Clazz clazz = (Clazz) it.next();

			if (!pAll.contains(clazz)) {
				pAll.add(clazz);
				clazz.findTransitiveDependencies(pAll);
			}
		}
	}

	public boolean equals(Object pO) {
		if (pO.getClass() != Clazz.class) {
			return false;
		}
		final Clazz c = (Clazz) pO;
		return name.equals(c.name);
	}

	public int hashCode() {
		return name.hashCode();
	}

	public int compareTo(Object pO) {
		return name.compareTo(((Clazz) pO).name);
	}

	public String toString() {
		return name;
	}

}
