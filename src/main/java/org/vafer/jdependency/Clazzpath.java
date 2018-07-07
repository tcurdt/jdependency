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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.apache.commons.io.FilenameUtils;
import org.objectweb.asm.ClassReader;
import org.vafer.jdependency.asm.DependenciesClassAdapter;

public final class Clazzpath {

    private static final class Resource {
        final String name;
        final InputStream inputStream;

        Resource( String pName , final InputStream inputStream) {
            this.name = pName.substring(0, pName.length() - 6).replace('/', '.');
            this.inputStream = inputStream;
        }

        static boolean isValidName( String pName ) {
            return pName != null && pName.endsWith(".class") && !pName.contains( "-" );
        }
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
                // missing.put(clazz.toString(), clazz);
            }
        }

        return units.remove(pUnit);
    }

    /**
     * Add a {@link ClazzpathUnit} to this {@link Clazzpath}.
     * @param pFile may be a directory or a jar file
     * @return newly created {@link ClazzpathUnit} with id of pFile.absolutePath
     * @throws IOException
     */
    public ClazzpathUnit addClazzpathUnit(final File pFile) throws IOException {
        return addClazzpathUnit(pFile.toPath());
    }

    /**
     * Add a {@link ClazzpathUnit} to this {@link Clazzpath}.
     * @param pFile may be a directory or a jar file
     * @return newly created {@link ClazzpathUnit} with id of pFile.absolutePath
     * @throws IOException
     */
    public ClazzpathUnit addClazzpathUnit(final Path pFile) throws IOException {
        return addClazzpathUnit(pFile, pFile.toAbsolutePath().toString());
    }

    public ClazzpathUnit addClazzpathUnit(final File pFile, final String pId) throws IOException {
        return addClazzpathUnit(pFile.toPath(), pId);
    }

    public ClazzpathUnit addClazzpathUnit(final Path pFile, final String pId) throws IOException {
        if (Files.isRegularFile(pFile) ) {
            return addClazzpathUnit( Files.newInputStream(pFile), pId);
        }else if (Files.isDirectory(pFile)) {
            final String prefix =
                FilenameUtils.separatorsToUnix(FilenameUtils
                    .normalize(new StringBuilder(pFile.toAbsolutePath().toString())
                        .append(File.separatorChar).toString()));

            final Iterable<Resource> resources = Files.walk(pFile)
                    .filter(Files::isRegularFile)
                    .filter(p -> FilenameUtils.isExtension(p.toString(), ".class"))
                    .map(p -> {
                        try {
                            final InputStream inputStream = Files.newInputStream(p);
                            return new Resource(p.toAbsolutePath().toString().substring(prefix.length()), inputStream);
                        } catch (final IOException e) {
                            throw new RuntimeException(e);
                        }
                    })::iterator;

            return addClazzpathUnit(resources, pId, true);
        } else {
            throw new IllegalArgumentException(String.format("Path: '%s' is neither a regular file or directory", pFile));
        }
    }

    public ClazzpathUnit addClazzpathUnit(final InputStream pInputStream, final String pId) throws IOException {

        try(final JarInputStream inputStream = new JarInputStream(pInputStream)) {
            final JarEntry[] entryHolder = new JarEntry[1];

            return addClazzpathUnit(new Iterable<Resource>() {

                public Iterator<Resource> iterator() {
                    return new Iterator<Resource>() {

                        public boolean hasNext() {
                            try {
                                do {
                                    entryHolder[0] = inputStream.getNextJarEntry();
                                } while (entryHolder[0] != null && !Resource.isValidName(entryHolder[0].getName()));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            return entryHolder[0] != null;
                        }

                        public Resource next() {
                            return new Resource(entryHolder[0].getName(), inputStream);
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }

                    };
                }
            }, pId, false);
        }
    }

    private ClazzpathUnit addClazzpathUnit(final Iterable<Resource> resources,
                                           final String pId,
                                           boolean shouldCloseResourceStream) throws IOException {
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
            final InputStream inputStream = resource.inputStream;
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
