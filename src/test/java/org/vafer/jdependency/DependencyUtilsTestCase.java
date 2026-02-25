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

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.vafer.jdependency.utils.DependencyUtils;

public final class DependencyUtilsTestCase {

    public static int getJavaVersion() {
        return parseVersion(System.getProperty("java.version"));
    }

    public static int parseVersion(String version) {
        String[] tokens = version.split("[.-]");

        if (tokens.length < 1) {
            return 0;
        }
        int major = Integer.parseInt(tokens[0]);

        if (major != 1) {
            return major;
        }

        if (tokens.length < 2) {
            return 0;
        }
        int minor = Integer.parseInt(tokens[1]);

        return minor;
    }

    @Test
    public void testVersions() {
        assertEquals(11, parseVersion("11"));
        assertEquals(11, parseVersion("11-ea"));
        assertEquals(11, parseVersion("11.0.2"));
        assertEquals(8, parseVersion("1.8.0_345"));

    }

    @Test
    public void testShouldFindDependenciesOfClassObject() throws Exception {
        final Set<String> dependencies = DependencyUtils.getDependenciesOfClass(Object.class);
        final Set<String> expectedDependencies = new HashSet<String>(Arrays.asList(
                "java.lang.Class",
                "java.lang.CloneNotSupportedException",
                "java.lang.IllegalArgumentException",
                "java.lang.Integer",
                "java.lang.InterruptedException",
                "java.lang.Object",
                "java.lang.String",
                "java.lang.StringBuilder",
                "java.lang.Thread",
                "java.lang.Throwable"
                ));

        final int jdk = getJavaVersion();

        if (jdk >= 9) {
            expectedDependencies.add("java.lang.Deprecated");
        }

        if (jdk >=9 && jdk <= 15) {
            expectedDependencies.add("jdk.internal.HotSpotIntrinsicCandidate");
        }

        if (jdk > 15) {
            expectedDependencies.add("jdk.internal.vm.annotation.IntrinsicCandidate");
        }

        if (jdk > 16) {
            expectedDependencies.add("jdk.internal.misc.Blocker");
        }

        for (String optionalDep : Arrays.asList("jdk.internal.misc.Blocker", "java.lang.Long", "java.lang.VirtualThread", "java.lang.Thread")) {
            if (dependencies.contains(optionalDep)) {
                expectedDependencies.add(optionalDep);
            } else {
                expectedDependencies.remove(optionalDep);
            }
        }

        assertEquals("deps should be the same for jdk " + jdk + " (" + System.getProperty("java.version") + ")",
            expectedDependencies,
            dependencies);
    }

    @Test
    public void testShouldFindDependenciesOfTestCaseClass() throws Exception {
        final Set<String> dependencies = DependencyUtils.getDependenciesOfClass(DependencyUtilsTestCase.class);
        assertTrue(dependencies.contains("org.vafer.jdependency.utils.DependencyUtils"));
        assertTrue(dependencies.contains("org.junit.Test"));
    }

    @Test
    public void testShouldThrowOnInvalidStream() throws Exception {
        assertThrows(IOException.class, () -> {
            final InputStream inputStream = new FileInputStream("nope");
            final Set<String> dependencies = DependencyUtils.getDependenciesOfClass(inputStream);
        });
    }

    private static interface DummyInterface {}

    @Test
    public void testShouldFindDependenciesOfInterface() throws Exception {
        final Set<String> dependencies = DependencyUtils.getDependenciesOfClass(DummyInterface.class);
        assertTrue(dependencies.contains("java.lang.Object"));
        assertTrue(dependencies.contains("org.vafer.jdependency.DependencyUtilsTestCase$DummyInterface"));
    }

    @Test
    public void testShouldThrowOnPrimitiveClass() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            DependencyUtils.getDependenciesOfClass(int.class);
        });
        assertTrue(exception instanceof NullPointerException || exception instanceof IOException);
    }

    @Test
    public void testShouldThrowOnNullClass() throws Exception {
        assertThrows(NullPointerException.class, () -> {
            DependencyUtils.getDependenciesOfClass((Class<?>) null);
        });
    }

    @Test
    public void testShouldThrowOnNullStream() throws Exception {
        Exception exception = assertThrows(Exception.class, () -> {
            DependencyUtils.getDependenciesOfClass((InputStream) null);
        });
        assertTrue(exception instanceof NullPointerException || exception instanceof IOException);
    }

}
