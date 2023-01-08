/*
 * Copyright 2010-2023 The jdependency developers.
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

import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.io.IOException;

/**
 * internal - do not use
 */

public final class StreamUtils {

    private StreamUtils() {}

    // public static <T> Stream<T> asStream(Iterator<T> sourceIterator) {
    //     return asStream(sourceIterator, false);
    // }

    // public static <T> Stream<T> asStream(Iterator<T> sourceIterator, boolean parallel) {
    //     Iterable<T> iterable = () -> sourceIterator;
    //     return StreamSupport.stream(iterable.spliterator(), parallel);
    // }

    public static Stream<JarEntry> asStream( final JarInputStream pInputStream ) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
            new Iterator<JarEntry>() {

                JarEntry entry = null;

                public boolean hasNext() {
                    try {
                        if (entry == null) {
                            entry = pInputStream.getNextJarEntry();
                        }
                        return entry != null;
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                public JarEntry next() {
                    try {
                        JarEntry result = entry != null
                          ? entry
                          : pInputStream.getNextJarEntry();
                        entry = null;
                        return result;
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                }

            }, Spliterator.IMMUTABLE), false);
    }
}