/*
 * Copyright 2010-2024 The jdependency developers.
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FilenameUtils.separatorsToUnix;

/**
 * A `Clazz` represents the single class identifier inside a classpath.
 * There is only one `Clazz` per classname. It has incoming and outgoing
 * edges defining references and dependencies. If there are different
 * versions found, it collects their sources as ClazzpathUnits.
 */
public final class Clazz implements Comparable<Clazz> {

    private final Set<Clazz> dependencies = new HashSet<>();
    private final Set<Clazz> references = new HashSet<>();
    private final Map<ClazzpathUnit, String> units = new HashMap<>();

    public static final class ClazzFile {
        private ClazzpathUnit unit;
        private String filename;

        public ClazzFile(ClazzpathUnit unit, String filename) {
            this.unit = unit;
            this.filename = filename;
        }

        public ClazzpathUnit getUnit() {
            return unit;
        }

        public String getFilename() {
            return filename;
        }

        @Override
        public String toString() {
            return "ClazzFile{" +
                    "unit=" + unit +
                    ", filename='" + filename + '\'' +
                    '}';
        }
    }

    // Usually a class is only in a single file.
    // When using MultiRelease Jar files this can be multiple files, one for each java release specified.
    // The default filename is under the key "8".
    private final Map<String, ClazzFile> classFilenames = new HashMap<>();

    // The name of the class (like "org.vafer.jdependency.Clazz")
    private final String name;

    public Clazz( final String pName ) {
        name = pName;
    }

    private static final Pattern EXTRACT_MULTI_RELEASE_JAVA_VERSION = Pattern.compile("^(?:META-INF[\\/\\\\]versions[\\/\\\\](\\d+)[\\/\\\\])?([^.]+).class$");

    public static final class ParsedFileName {
        public String className;
        public String forJava;

        @Override
        public String toString() {
            return "ParsedFileName{" +
                    "className='" + className + '\'' +
                    ", forJava='" + forJava + '\'' +
                    '}';
        }
    }

    /**
     * Determine the class name for the provided filename.
     *
     * @param pFileName The filename
     * @return the class name for the provided filename OR null if it is not a .class file.
     */
    public static ParsedFileName parseClassFileName(String pFileName) {
        if (pFileName == null || !pFileName.endsWith(".class")) {
            return null;// Not a class filename
        }
        // foo/bar/Foo.class -> // foo.bar.Foo

        Matcher matcher = EXTRACT_MULTI_RELEASE_JAVA_VERSION.matcher(pFileName);
        if (!matcher.matches()) {
            return null;
        }
        ParsedFileName result = new ParsedFileName();
        result.forJava = matcher.group(1);
        result.className = separatorsToUnix(matcher.group(2)).replace('/', '.');

        if (result.forJava == null || result.forJava.isEmpty()) {
            if (result.className.contains("-")) {
                return null;
            }
            result.forJava = "8";
        }

        return result;
    }

    /**
     * Determine if the provided filename is the name of a class that is specific for a java version.
     * @param pFileName The filename to be evaluated
     * @return true if this is a filename for a specific java version, false if it is not
     */
    public static boolean isMultiReleaseClassFile(String pFileName) {
        if (pFileName == null) {
            return false;
        }
        Matcher matcher = EXTRACT_MULTI_RELEASE_JAVA_VERSION.matcher(pFileName);
        if (!matcher.matches()) {
            return false;
        }
        return matcher.group(1) != null && !matcher.group(1).isEmpty();
    }

    /**
     * Record that this class name can be found at:
     * @param pUnit The unit in which the class can be found
     * @param pForJava For which Java version
     * @param pFileName Under which filename in the jar.
     */
    public void addMultiReleaseFile(ClazzpathUnit pUnit, String pForJava, String pFileName) {
        classFilenames.put(pForJava, new ClazzFile(pUnit, pFileName));
    }

    public String getName() {
        return name;
    }

    public Map<String, ClazzFile> getFileNames() {
        return classFilenames;
    }

    public void addClazzpathUnit( final ClazzpathUnit pUnit, final String pDigest ) {
        units.put(pUnit, pDigest);
    }

    public void removeClazzpathUnit( final ClazzpathUnit pUnit ) {
        units.remove(pUnit);
    }

    public Set<ClazzpathUnit> getClazzpathUnits() {
        return units.keySet();
    }

    public Set<String> getVersions() {
        // System.out.println("clazz:" + name + " units:" + units);
        return new HashSet<>(units.values());
    }


    public void addDependency( final Clazz pClazz ) {
        pClazz.references.add(this);
        dependencies.add(pClazz);
    }

    public void removeDependency( final Clazz pClazz ) {
        pClazz.references.remove(this);
        dependencies.remove(pClazz);
    }

    public Set<Clazz> getDependencies() {
        return dependencies;
    }



    public Set<Clazz> getReferences() {
        return references;
    }


    public Set<Clazz> getTransitiveDependencies() {
        final Set<Clazz> all = new HashSet<>();
        findTransitiveDependencies(all);
        return all;
    }


    void findTransitiveDependencies( final Set<? super Clazz> pAll ) {

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

    public int compareTo( final Clazz pO ) {
        return name.compareTo(((Clazz) pO).name);
    }

    public String toString() {
        return name + " in " + classFilenames;
    }

}
