/*
 * Copyright 2010-2020 The jdependency developers.
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
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;

import org.vafer.jdependency.utils.ClazzUtils;

public final class ClazzUtilsTestCase {

    private static Path resourcePath( String filename ) {
        return Paths.get(filename);
    }

    @Test
    public void testUniq() throws IOException {

        final Clazzpath cp = new Clazzpath();

        final Path jar1 = resourcePath("jaxb-impl-2.3.1.jar");
        cp.addClazzpathUnit(jar1, jar1.toAbsolutePath().toString());

        final Path jar2 = resourcePath("jaxb-runtime-2.3.1.jar");
        cp.addClazzpathUnit(jar2, jar2.toAbsolutePath().toString());

        Set<Clazz> clashed = cp.getClashedClazzes();
        assertTrue(!clashed.isEmpty());

        Set<Clazz> uniq = ClazzUtils.uniq(clashed);
        assertTrue(uniq.isEmpty());
    }


}
