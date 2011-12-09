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
package org.vafer.jdependency.asm;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.commons.RemappingClassAdapter;

public final class DependenciesClassAdapter extends RemappingClassAdapter {

    final Set<String> classes = new HashSet<String>();

    public DependenciesClassAdapter() {
        super(new EmptyVisitor(), new CollectingRemapper());
    }
    
    public Set<String> getDependencies() {
        return ((CollectingRemapper) super.remapper).classes;
    }

    private static class CollectingRemapper extends Remapper {

        final Set<String> classes = new HashSet<String>();

        public String map(String pClassName) {
            classes.add(pClassName.replace('/', '.'));
            return pClassName;
        }       
    }   
}
