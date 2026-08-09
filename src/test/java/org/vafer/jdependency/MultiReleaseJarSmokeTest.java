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

import java.io.IOException;
import java.util.Set;

import org.vafer.jdependency.utils.DependencyUtils;

/**
 * Smoke test executed against the packaged JAR, rather than the build output
 * directories used by Surefire.
 */
public final class MultiReleaseJarSmokeTest {

    private MultiReleaseJarSmokeTest() {}

    public static void main(final String[] args) throws IOException {
        final String resource = DependencyUtils.class
            .getResource("DependencyUtils.class")
            .toExternalForm();
        final boolean versioned = resource.contains("META-INF/versions/24/");
        final int javaVersion = javaVersion();

        if (versioned != (javaVersion >= 24)) {
            throw new AssertionError("unexpected DependencyUtils resource for Java "
                + javaVersion + ": " + resource);
        }

        final Set<String> dependencies = DependencyUtils
            .getDependenciesOfClass(MultiReleaseJarSmokeTest.class);
        if (!dependencies.contains(MultiReleaseJarSmokeTest.class.getName())) {
            throw new AssertionError("DependencyUtils did not analyze the packaged JAR");
        }
    }

    private static int javaVersion() {
        final String version = System.getProperty("java.specification.version");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2));
        }
        final int dot = version.indexOf('.');
        return Integer.parseInt(dot < 0 ? version : version.substring(0, dot));
    }
}
