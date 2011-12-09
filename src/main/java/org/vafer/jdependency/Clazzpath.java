/*
 * Copyright 2010-2011 The Apache Software Foundation.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.vafer.jdependency.asm.DependenciesClassAdapter;

public final class Clazzpath {

    private final Set<ClazzpathUnit> units = new HashSet<ClazzpathUnit>();
    private final Map<String, Clazz> missing = new HashMap<String, Clazz>();
    private final Map<String, Clazz> clazzes = new HashMap<String, Clazz>();

    public Clazzpath() {
    }

    public boolean removeClazzpathUnit( final ClazzpathUnit pUnit ) {
        
        final Set<Clazz> unitClazzes = pUnit.getClazzes();

        for (Clazz clazz : unitClazzes) {
            clazz.removeClazzpathUnit(pUnit);
            if (clazz.getClazzpathUnits().size() == 0) {
                clazzes.remove(clazz.toString());
                // missing.put(clazz.toString(), clazz);
            }
        }
        
        return units.remove(pUnit);
    }

    public ClazzpathUnit addClazzpathUnit( final InputStream pInputStream, final String pId ) throws IOException {

        final Map<String, Clazz> unitClazzes = new HashMap<String, Clazz>();
        final Map<String, Clazz> unitDependencies = new HashMap<String, Clazz>();

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
                    clazz = missing.get(clazzName);

                    if (clazz != null) {
                        // already marked missing
                        clazz = missing.remove(clazzName);
                    } else {
                        clazz = new Clazz(clazzName);
                    }
                }
                
                clazz.addClazzpathUnit(unit);

                clazzes.put(clazzName, clazz);
                unitClazzes.put(clazzName, clazz);

                final DependenciesClassAdapter v = new DependenciesClassAdapter();
                new ClassReader(inputStream).accept(v, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG);
                final Set<String> depNames = v.getDependencies();

                for (String depName : depNames) {

                    Clazz dep = getClazz(depName);

                    if (dep == null) {
                        // there is no such clazz yet
                        dep = missing.get(depName);
                    }

                    if (dep == null) {
                        // it is also not recorded to be missing
                        dep = new Clazz(depName);
                        dep.addClazzpathUnit(unit);
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

    public Set<Clazz> getClazzes() {
        final Set<Clazz> result = new HashSet<Clazz>(clazzes.values());
        return result;
    }

    public Set<Clazz> getClashedClazzes() {
        final Set<Clazz> all = new HashSet<Clazz>();
        for (Clazz clazz : clazzes.values()) {          
            if (clazz.getClazzpathUnits().size() > 1) {
                all.add(clazz);
            }
        }
        return all; 
    }

    public Set<Clazz> getMissingClazzes() {
        final Set<Clazz> result = new HashSet<Clazz>(missing.values());
        return result;
    }
    
    public Clazz getClazz(final String pClazzName) {
        final Clazz result = (Clazz) clazzes.get(pClazzName);
        return result;
    }

    public ClazzpathUnit[] getUnits() {
        final ClazzpathUnit[] result = units.toArray(new ClazzpathUnit[units.size()]);
        return result;
    }

}
