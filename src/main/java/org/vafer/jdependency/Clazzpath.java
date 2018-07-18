/*
 * Copyright 2010-2018 The jdependency developers.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;
import java.nio.file.Files;
import java.lang.IllegalArgumentException;
import org.objectweb.asm.ClassReader;

import org.vafer.jdependency.asm.DependenciesClassAdapter;

import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

import static org.vafer.jdependency.utils.StreamUtils.asStream;



public final class Clazzpath {

    private static abstract class Resource {

        final String name;

        Resource( String pName ) {
            super();

            final int len = pName.length();

            // if (len < 6+1) {
            //     throw new IllegalArgumentException("resource name is too short: [" + pName + "]");
            // }

            // foo/bar/Foo.class -> // foo.bar.Foo
            this.name = pName
                .substring(0, len - 6)
                .replace('/', '.');
                // .replace('\\', '.');
                // .replace(File.separatorChar, '.');
        }

        abstract InputStream getInputStream() throws IOException;
    }

    private static boolean isValidResourceName( String pName ) {
        return pName != null
            && pName.endsWith(".class")
            && !pName.contains( "-" );
    }

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
            }
        }

        return units.remove(pUnit);
    }

    public final ClazzpathUnit addClazzpathUnit( final File pFile ) throws IOException {
        return addClazzpathUnit(pFile, pFile.getAbsolutePath());
    }

    public ClazzpathUnit addClazzpathUnit( final File pFile, final String pId ) throws IOException {

        if (pFile.isFile()) {

            return addClazzpathUnit(new FileInputStream(pFile), pId);

        } else if (pFile.isDirectory()) {

            final String prefix = separatorsToUnix(normalize(pFile.getAbsolutePath() + File.separatorChar));

            Iterable<Resource> resources = Files.walk(pFile.toPath())
                .filter(p -> Files.isRegularFile(p))
                .filter(p -> isValidResourceName(p.getFileName().toString()))
                .map(p -> p.toString())
                .map(abs -> (Resource) new Resource(abs.substring(prefix.length())) {
                    InputStream getInputStream() throws IOException {
                        return new FileInputStream(abs);
                    }
                })::iterator;

            return addClazzpathUnit(resources, pId, true);
        }
        throw new IllegalArgumentException("neither file nor directory");
    }

    public ClazzpathUnit addClazzpathUnit(final InputStream pInputStream, final String pId) throws IOException {

        final JarInputStream inputStream = new JarInputStream(pInputStream);

        try {

            Iterable<Resource> resources = asStream(inputStream)
                .map(e -> e.getName())
                .filter(name -> isValidResourceName(name))
                .map(name -> (Resource) new Resource(name) {
                    InputStream getInputStream() throws IOException {
                        return inputStream;
                    }
                })::iterator;

           return addClazzpathUnit(resources, pId, false);

        } finally {
            inputStream.close();
        }
    }

    private ClazzpathUnit addClazzpathUnit( final Iterable<Resource> resources, final String pId, boolean shouldCloseResourceStream ) throws IOException {

        final Map<String, Clazz> unitClazzes = new HashMap<String, Clazz>();
        final Map<String, Clazz> unitDependencies = new HashMap<String, Clazz>();

        final ClazzpathUnit unit = new ClazzpathUnit(pId, unitClazzes, unitDependencies);

        for (Resource resource : resources) {

            final String clazzName = resource.name;

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
            final InputStream inputStream = resource.getInputStream();
            try {
                new ClassReader(inputStream).accept(v, ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG);
            } finally {
                if (shouldCloseResourceStream) inputStream.close();
            }

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

        units.add(unit);

        return unit;
    }

    public Set<Clazz> getClazzes() {
        return new HashSet<Clazz>(clazzes.values());
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
        return new HashSet<Clazz>(missing.values());
    }

    public Clazz getClazz(final String pClazzName) {
        return (Clazz) clazzes.get(pClazzName);
    }

    public ClazzpathUnit[] getUnits() {
        return units.toArray(new ClazzpathUnit[units.size()]);
    }

}
