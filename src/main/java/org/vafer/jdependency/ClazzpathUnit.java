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
import java.util.Map;
import java.util.Set;

public final class ClazzpathUnit {

	private final String id;

	private final Map<String, Clazz> clazzes;
	private final Map<String, Clazz> dependencies;

	ClazzpathUnit( final String pId, final Map pClazzes, final Map pDependencies ) {
		id = pId;
		clazzes = pClazzes;
		dependencies = pDependencies;
	}

	public Set getClazzes() {
		final Set all = new HashSet();
		for (Clazz clazz : clazzes.values()) {
			all.add(clazz);
		}
		return all;
	}

	public Clazz getClazz( final String pClazzName ) {
		return (Clazz) clazzes.get(pClazzName);
	}

	public Set getDependencies() {
		final Set all = new HashSet();
		for (Clazz clazz : dependencies.values()) {
			all.add(clazz);
		}
		return all;
	}

	public Set getTransitiveDependencies() {
		final Set all = new HashSet();
		for (Clazz clazz : clazzes.values()) {
			clazz.findTransitiveDependencies(all);
		}
		return all;
	}

	public String toString() {
		return id;
	}
}
