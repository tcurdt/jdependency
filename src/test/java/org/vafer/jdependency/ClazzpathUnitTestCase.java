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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ClazzpathUnitTestCase {

    private static Path resourcePath( String filename ) {
        return Paths.get(filename);
    }

    // mvn is copying all into the test working dir
    private static File resourceFile( String filename ) {
        return Paths.get(filename).toFile();
    }

    @Test
    public void testShouldAddClasses() throws IOException {

        final Clazzpath cp = new Clazzpath();

        final ClazzpathUnit u1w = cp.addClazzpathUnit(resourceFile("jar1.jar"));
        assertEquals(u1w.toString(), "jar1.jar");

        final ClazzpathUnit u1wo = cp.addClazzpathUnit(resourceFile("jar1.jar"), "jar1");
        assertEquals(u1wo.toString(), "jar1");

        final ClazzpathUnit u2w = cp.addClazzpathUnit(resourcePath("jar2.jar"));
        assertEquals(u2w.toString(), "jar2.jar");

        final ClazzpathUnit u2wo = cp.addClazzpathUnit(resourcePath("jar2.jar"), "jar2");
        assertEquals(u2wo.toString(), "jar2");
    }

    @Test
    public void testDependencies() throws IOException {

        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit u = cp.addClazzpathUnit(resourceFile("jar1.jar"));

        final Set<Clazz> deps = u.getDependencies();
        assertEquals(116, deps.size());

        final Set<Clazz> transitiveDeps = u.getTransitiveDependencies();
        assertEquals(116, transitiveDeps.size());

    }

}
