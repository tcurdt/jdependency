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
package org.vafer.jdependency.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.vafer.jdependency.asm.DependenciesClassAdapter;

/**
 * internal - do not use
 */
public final class DependencyUtils {

    private DependencyUtils() {}

    /*
    public static Set<String> getDependenciesOfJar( final InputStream pInputStream ) throws IOException {

        final JarInputStream inputStream = new JarInputStream(pInputStream);
        final NullOutputStream nullStream = new NullOutputStream();
        final Set<String> dependencies = new HashSet<String>();

        try {
            while (true) {
                final JarEntry entry = inputStream.getNextJarEntry();

                if (entry == null) {
                    break;
                }

                if (entry.isDirectory()) {
                    // ignore directory entries
                    IOUtils.copy(inputStream, nullStream);
                    continue;
                }

                final String name = entry.getName();

                if (name.endsWith(".class")) {
                    final DependenciesClassAdapter v = new DependenciesClassAdapter();
                    new ClassReader( inputStream ).accept( v, 0 );
                    dependencies.addAll(v.getDependencies());
                } else {
                    IOUtils.copy(inputStream, nullStream);
                }
            }
        } finally {
            inputStream.close();
        }

        return dependencies;
    }
    */

    public static Set<String> getDependenciesOfClass( final InputStream pInputStream ) throws IOException {
        final DependenciesClassAdapter v = new DependenciesClassAdapter();
        new ClassReader( pInputStream ).accept( v, ClassReader.EXPAND_FRAMES );
        return v.getDependencies();
    }

    public static Set<String> getDependenciesOfClass( final Class<?> pClass ) throws IOException {
        final String resource = "/" + pClass.getName().replace('.', '/') + ".class";
        return getDependenciesOfClass(pClass.getResourceAsStream(resource));
    }

}
