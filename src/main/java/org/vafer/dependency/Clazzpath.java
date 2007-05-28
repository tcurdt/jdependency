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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.vafer.dependency.asm.CollectingDependencyVisitor;

public final class Clazzpath {

	public interface ClashHandler {
		
		void handleClash( final Clazz pClazz );
		
	}
	
	private final Set units = new HashSet();

	private final Map missing = new HashMap();

	private final Map clazzes = new HashMap();

	public Clazzpath() {
	}

	public boolean removeClazzpathUnit( final ClazzpathUnit pUnit ) {
		// FIXME: remove classes and adjust missing		
		return units.remove(pUnit);
	}

	public ClazzpathUnit addClazzpathUnit( final InputStream pInputStream, final String pId, final ClashHandler pClashHandler ) throws IOException {

		final Map unitClazzes = new HashMap();
		final Map unitDependencies = new HashMap();

		final ClazzpathUnit unit = new ClazzpathUnit(pId, unitClazzes, unitDependencies);
		
		final JarInputStream inputStream = new JarInputStream(pInputStream);
		
        while (true) {
            final JarEntry entry = inputStream.getNextJarEntry();
            
            if (entry == null) {
                break;
            }

			final String entryName = entry.getName();
			if (entryName.endsWith(".class")) {

				final String clazzName = entryName.substring(0, entryName.length() - 6).replace('/', '.');

				Clazz clazz = getClazz(clazzName);

				if (clazz == null) {
					clazz = (Clazz) missing.get(clazzName);

					if (clazz != null) {
						// already marked missing
						clazz = (Clazz) missing.remove(clazzName);
					} else {
						clazz = new Clazz(unit, clazzName);
					}
				} else {
					// classpath clash
					pClashHandler.handleClash(clazz);
				}

				clazzes.put(clazzName, clazz);
				unitClazzes.put(clazzName, clazz);

				final CollectingDependencyVisitor v = new CollectingDependencyVisitor();
				new ClassReader(inputStream).accept(v, false);
				final Set depNames = v.getDependencies();

				for (final Iterator it = depNames.iterator(); it.hasNext();) {
					final String depName = (String) it.next();

					Clazz dep = getClazz(depName);

					if (dep == null) {
						// there is no such clazz yet
						dep = (Clazz) missing.get(depName);
					}

					if (dep == null) {
						// it is also not recorded to be missing
						dep = new Clazz(unit, depName);
						missing.put(depName, dep);
					}

					if (dep != clazz) {
						unitDependencies.put(depName, dep);
						clazz.addDependency(dep);
					}
				}
			}
		}

		units.add(unit);

		return unit;
	}

	public Set getClazzes() {
		final Set all = new HashSet();
		for (final Iterator it = clazzes.values().iterator(); it.hasNext();) {
			final Clazz clazz = (Clazz) it.next();
			all.add(clazz);
		}
		return all;
	}

	public Set getMissingClazzes() {
		final Set all = new HashSet();
		for (final Iterator it = missing.values().iterator(); it.hasNext();) {
			final Clazz clazz = (Clazz) it.next();
			all.add(clazz);
		}
		return all;
	}

	public Clazz getClazz(final String pClazzName) {
		return (Clazz) clazzes.get(pClazzName);
	}

	public ClazzpathUnit[] getUnits() {
		return (ClazzpathUnit[]) units.toArray(new ClazzpathUnit[units.size()]);
	}

}
