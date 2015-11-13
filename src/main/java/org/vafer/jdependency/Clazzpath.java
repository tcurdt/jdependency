/*
 * Copyright 2010-2015 The jdependency developers.
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.objectweb.asm.ClassReader;
import org.vafer.jdependency.asm.DependenciesClassAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public final class Clazzpath {

    private static abstract class Resource {
        final String name;

        Resource( String pName ) {
            super();
            this.name = pName.substring(0, pName.length() - 6).replace('/', '.');
        }

        abstract InputStream getInputStream() throws IOException;

        static boolean isValidName( String pName ) {
            return pName != null && pName.endsWith(".class");
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
    public final ClazzpathUnit addClazzpathUnit( final File pFile ) throws IOException {
        return addClazzpathUnit(pFile, pFile.getAbsolutePath());
    }

    public ClazzpathUnit addClazzpathUnit( final File pFile, final String pId ) throws IOException {
        if ( pFile.isFile() ) {
            return addClazzpathUnit( new FileInputStream(pFile), pId);
        }
        if (pFile.isDirectory()) {
            final String prefix =
                FilenameUtils.separatorsToUnix(FilenameUtils
                    .normalize(new StringBuilder(pFile.getAbsolutePath())
                        .append(File.separatorChar).toString()));
            final boolean recursive = true;
            @SuppressWarnings("unchecked")
            final Iterator<File> files = FileUtils.iterateFiles(pFile, new String[] { "class" }, recursive);
            return addClazzpathUnit( new Iterable<Resource>() {

                public Iterator<Resource> iterator() {
                    return new Iterator<Clazzpath.Resource>() {

                        public boolean hasNext() {
                            return files.hasNext();
                        }

                        public Resource next() {
                            final File file = files.next();
                            return new Resource(file.getAbsolutePath().substring(prefix.length())) {

                                @Override
                                InputStream getInputStream() throws IOException {
                                    return new FileInputStream(file);
                                }
                            };
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }

            }, pId, true);
        }
        throw new IllegalArgumentException();
    }

    public ClazzpathUnit addClazzpathUnit(final InputStream pInputStream, final String pId) throws IOException {
        final JarInputStream inputStream = new JarInputStream(pInputStream);
        try {
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
                            return new Resource(entryHolder[0].getName()) {

                                @Override
                                InputStream getInputStream() {
                                    return inputStream;
                                }
                            };
                        }

                        public void remove() {
                            throw new UnsupportedOperationException();
                        }

                    };
                }
            }, pId, false);
        } finally {
            inputStream.close();
        }
    }

    private ClazzpathUnit addClazzpathUnit(final Iterable<Resource> resources, final String pId, boolean shouldCloseResourceStream) throws IOException {
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
