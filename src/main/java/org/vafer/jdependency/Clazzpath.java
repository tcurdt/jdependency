/*
 * Copyright 2010-2019 The jdependency developers.
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

import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.vafer.jdependency.utils.StreamUtils.asStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.objectweb.asm.ClassReader;
import org.vafer.jdependency.asm.DependenciesClassAdapter;



public final class Clazzpath {

    private final Set<ClazzpathUnit> units = new HashSet<>();
    private final Map<String, Clazz> missing = new HashMap<>();
    private final Map<String, Clazz> clazzes = new HashMap<>();

    private abstract static class Resource {

    	private static final int EXT = ".class".length();

    	public final String name;

        Resource( final String pName ) {
            super();

            final int all = pName.length();

            // foo/bar/Foo.class -> // foo.bar.Foo
            this.name = separatorsToUnix(pName)
                .substring(0, all - EXT)
                .replace('/', '.');
        }

        abstract InputStream getInputStream() throws IOException;
    }

    private static boolean isValidResourceName( final String pName ) {
        return pName != null
            && pName.endsWith(".class")
            && !pName.contains( "-" );
    }

    public boolean removeClazzpathUnit( final ClazzpathUnit pUnit ) {

        final Set<Clazz> unitClazzes = pUnit.getClazzes();

        for (Clazz clazz : unitClazzes) {
            clazz.removeClazzpathUnit(pUnit);
            if (clazz.getClazzpathUnits().isEmpty()) {
                clazzes.remove(clazz.toString());
            }
        }

        return units.remove(pUnit);
    }

    public ClazzpathUnit addClazzpathUnit( final File pFile ) throws IOException {
        return addClazzpathUnit(pFile.toPath());
    }

    public ClazzpathUnit addClazzpathUnit( final File pFile, final String pId ) throws IOException {
        return addClazzpathUnit(pFile.toPath(), pId);
    }


    public ClazzpathUnit addClazzpathUnit( final Path pPath ) throws IOException {
        return addClazzpathUnit(pPath, pPath.toString());
    }

    public ClazzpathUnit addClazzpathUnit( final Path pPath, final String pId ) throws IOException {

        final Path path = pPath.toAbsolutePath();
        if (Files.isRegularFile(path)) {

            return addClazzpathUnit(Files.newInputStream(path), pId);

        } else if (Files.isDirectory(path)) {

            final String prefix = separatorsToUnix(normalize(path.toString() + '/'));

            Iterable<Resource> resources = Files.walk(path)
                .filter(Files::isRegularFile)
                .filter(p -> isValidResourceName(p.getFileName().toString()))
                .map(p -> (Resource) new Resource(p.toString().substring(prefix.length())) {
                    @Override
					InputStream getInputStream() throws IOException {
                        return Files.newInputStream(p);
                    }
                })::iterator;

            return addClazzpathUnit(resources, pId, true);
        }

        throw new IllegalArgumentException("neither file nor directory");
    }

    public ClazzpathUnit addClazzpathUnit( final InputStream pInputStream, final String pId ) throws IOException {
        try (final JarInputStream inputStream = new JarInputStream(pInputStream)){
            Iterable<Resource> resources = asStream(inputStream)
                .map(JarEntry::getName)
                .filter(Clazzpath::isValidResourceName)
                .map(name -> (Resource) new Resource(name) {
                    @Override
					InputStream getInputStream() throws IOException {
                        return inputStream;
                    }
                })::iterator;

           return addClazzpathUnit(resources, pId, false);
        }
    }

    private ClazzpathUnit addClazzpathUnit( final Iterable<Resource> resources, final String pId, boolean shouldCloseResourceStream ) throws IOException {

        final Map<String, Clazz> unitClazzes = new HashMap<>();
        final Map<String, Clazz> unitDependencies = new HashMap<>();

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

            /// add to classpath
            clazzes.put(clazzName, clazz);

            // add to classpath unit
            unitClazzes.put(clazzName, clazz);

            // extract dependencies of clazz
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
                    // add as missing
                    missing.put(depName, dep);
                }

                if (dep != clazz) {
                    // unit depends on dep
                    unitDependencies.put(depName, dep);
                    // clazz depends on dep
                    clazz.addDependency(dep);
                }
            }
        }

        units.add(unit);
        return unit;
    }

    public Set<Clazz> getClazzes() {
        return new HashSet<>(clazzes.values());
    }

    public Set<Clazz> getClashedClazzes() {
        final Set<Clazz> all = new HashSet<>();
        for (Clazz clazz : clazzes.values()) {
            if (clazz.getClazzpathUnits().size() > 1) {
                all.add(clazz);
            }
        }
        return all;
    }

    public Set<Clazz> getMissingClazzes() {
        return new HashSet<>(missing.values());
    }

    public Clazz getClazz( final String pClazzName ) {
        return clazzes.get(pClazzName);
    }

    public ClazzpathUnit[] getUnits() {
        return units.toArray(new ClazzpathUnit[units.size()]);
    }
}
