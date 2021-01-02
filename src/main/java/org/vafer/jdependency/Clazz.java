/*
 * Copyright 2010-2021 The jdependency developers.
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

    private final String name;

    public Clazz( final String pName ) {
        name = pName;
    }

    public String getName() {
        return name;
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
        return name;
    }

}
