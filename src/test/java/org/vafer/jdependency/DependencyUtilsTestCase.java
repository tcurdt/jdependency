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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.vafer.jdependency.utils.DependencyUtils;

public final class DependencyUtilsTestCase {

    public static int getJavaVersion() {
        return parseVersion(System.getProperty("java.version"));
    }

    public static int parseVersion(String version) {
        int dotPos = version.indexOf('.');
        int dashPos = version.indexOf('-');
        int len = version.length();
        String major = version.substring(0, dotPos > -1 ? dotPos : dashPos > -1 ? dashPos : len);
        return Integer.parseInt(major);
    }

    @Test
    public void testVersions() {
        assertEquals(11, parseVersion("11"));
        assertEquals(11, parseVersion("11-ea"));
        assertEquals(11, parseVersion("11.0.2"));
    }

    @Test
    public void testShouldFindDependenciesOfClassObject() throws Exception {
        final Set<String> dependencies = DependencyUtils.getDependenciesOfClass(Object.class);
        final Set<String> expectedDependencies = new HashSet<>(Arrays.asList(
                "java.lang.String",
                "java.lang.IllegalArgumentException",
                "java.lang.CloneNotSupportedException",
                "java.lang.Class",
                "java.lang.InterruptedException",
                "java.lang.Integer",
                "java.lang.Object",
                "java.lang.StringBuilder",
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

        assertEquals("deps should be the same for jdk " + jdk, expectedDependencies, dependencies);
    }
}
