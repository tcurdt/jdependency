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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ClazzpathTestCase {

    private final AddClazzpathUnit addClazzpathUnit;

    private static abstract class AddClazzpathUnit {

        abstract ClazzpathUnit to( Clazzpath clazzpath, String filename, String id ) throws IOException;

        final ClazzpathUnit to( Clazzpath clazzpath, String filename ) throws IOException {
            return to(clazzpath, filename, filename);
        }
    }

    private final String kind;


    private static Path resourcePath( String filename ) {
        return Paths.get(filename);
    }

    private static File resourceFile( String filename ) {
        return Paths.get(filename).toFile();
    }

    private static Set<ClazzpathUnit> unitSet(ClazzpathUnit[] ar) {
        return new HashSet<>(Arrays.asList(ar));
    }

    /**
     * Parameters for test
     *
     * 1. AddClazzpathUnit for classpath-based jars
     * 2. AddClazzpathUnit for filesystem-based jars
     * 3. AddClazzpathUnit for filesystem-based directories
     */
    @Parameters(name = "{index}: {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(

            new Object[] { new AddClazzpathUnit() {

                @Override
				ClazzpathUnit to(Clazzpath toClazzpath, String filename, String id) throws IOException {
                    final String p = filename + ".jar";
                    InputStream resourceAsStream = getClass()
                        .getClassLoader()
                        .getResourceAsStream(p);
                    assertNotNull("missing:" + p, resourceAsStream);
                    return toClazzpath.addClazzpathUnit(resourceAsStream, id);
                }
            }, "classpath"},
            new Object[] { new AddClazzpathUnit() {

                @Override
				ClazzpathUnit to(Clazzpath toClazzpath, String filename, String id) throws IOException {
                    final String p = filename + ".jar";
                    File file = resourceFile(p);
                    assertTrue("missing:" + file, file.exists() && file.isFile());
                    return toClazzpath.addClazzpathUnit(file, id);
                }
            }, "file-jar"},
            new Object[] { new AddClazzpathUnit() {

                @Override
				ClazzpathUnit to(Clazzpath toClazzpath, String filename, String id) throws IOException {
                    final String p = filename;
                    File file = resourceFile(p);
                    assertTrue("missing:" + file , file.exists() && file.isDirectory());
                    return toClazzpath.addClazzpathUnit(file, id);
                }
            }, "file-directory"},
            new Object[] { new AddClazzpathUnit() {

                @Override
				ClazzpathUnit to(Clazzpath toClazzpath, String filename, String id) throws IOException {
                    final String p = filename + ".jar";
                    Path path = resourcePath(p);
                    assertTrue("missing:" + path, Files.exists(path) && Files.isRegularFile(path));
                    return toClazzpath.addClazzpathUnit(path, id);
                }
            }, "path-jar"},
            new Object[] { new AddClazzpathUnit() {

                @Override
				ClazzpathUnit to(Clazzpath toClazzpath, String filename, String id) throws IOException {
                    final String p = filename;
                    Path path = resourcePath(p);
                    assertTrue("missing:" + path, Files.exists(path) && Files.isDirectory(path));
                    return toClazzpath.addClazzpathUnit(path, id);
                }
            }, "path-directory"}

        );
    }

    public ClazzpathTestCase( AddClazzpathUnit pAddClazzpathUnit, String pKind ) {
        super();
        addClazzpathUnit = pAddClazzpathUnit;
        kind = pKind;
    }

    @Test
    public void testShouldAddClasses() throws IOException {

        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit u1 = addClazzpathUnit.to(cp, "jar1");
        final ClazzpathUnit u2 = addClazzpathUnit.to(cp, "jar2");

        final ClazzpathUnit[] units = cp.getUnits();

        assertEquals(2, units.length);
        assertEquals(unitSet(new ClazzpathUnit[]{ u1, u2 }), unitSet(units));

        assertEquals(129, cp.getClazzes().size());

        final Clazz clazz = cp.getClazz("org.apache.commons.io.IOUtils");
        assertNotNull(clazz);

        assertEquals(1, clazz.getClazzpathUnits().size());
        assertEquals(unitSet(new ClazzpathUnit[]{u1}), clazz.getClazzpathUnits());
        assertEquals(23, clazz.getDependencies().size());
        assertEquals(32, clazz.getTransitiveDependencies().size());
        assertEquals(5, clazz.getReferences().size());

    }

    @Test
    public void testShouldRemoveClasspathUnit() throws IOException {

        final Clazzpath cp = new Clazzpath();

        final ClazzpathUnit unit1 = addClazzpathUnit.to(cp, "jar1");

        assertEquals(59, cp.getClazzes().size());

        final ClazzpathUnit unit2 = addClazzpathUnit.to(cp, "jar2");

        assertEquals(129, cp.getClazzes().size());

        cp.removeClazzpathUnit(unit1);

        assertEquals(70, cp.getClazzes().size());

        cp.removeClazzpathUnit(unit2);

        assertEquals(0, cp.getClazzes().size());
    }

    @Test
    public void testShouldRevealMissingClasses() throws IOException {

        final Clazzpath cp = new Clazzpath();
        addClazzpathUnit.to(cp, "jar1-missing");

        final Set<Clazz> missing = cp.getMissingClazzes();

        final Set<String> actual = new HashSet<>();
        for (Clazz clazz : missing) {
            String name = clazz.getName();
            // ignore the rt
            if (!name.startsWith("java")) {
                actual.add(name);
            }
        }

        final Set<String> expected = new HashSet<>(Arrays.asList(
            "org.apache.commons.io.output.ProxyOutputStream",
            "org.apache.commons.io.input.ProxyInputStream"
            ));

        assertEquals(expected, actual);
    }

    @Test
    public void testShouldShowClasspathUnitsResponsibleForClash() throws IOException {

        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit a = addClazzpathUnit.to(cp, "jar1");
        final ClazzpathUnit b = addClazzpathUnit.to(cp, "jar1", "foo");

        final Set<Clazz> clashed = cp.getClashedClazzes();
        final Set<Clazz> all = cp.getClazzes();

        assertEquals(all, clashed);

        for (Clazz clazz : clashed) {
            assertTrue(clazz.getClazzpathUnits().contains(a));
            assertTrue(clazz.getClazzpathUnits().contains(b));
        }
    }

    @Test
    public void testShouldFindUnusedClasses() throws IOException {

        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit artifact = addClazzpathUnit.to(cp, "jar3using1");
        addClazzpathUnit.to(cp, "jar1");

        final Set<Clazz> removed = cp.getClazzes();
        removed.removeAll(artifact.getClazzes());
        removed.removeAll(artifact.getTransitiveDependencies());

        assertEquals("" + removed, 56, removed.size());

        final Set<Clazz> kept = cp.getClazzes();
        kept.removeAll(removed);

        assertEquals("" + kept, 4, kept.size());
    }

    @Test
    public void testWithModuleInfo() throws Exception {

        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit artifact = addClazzpathUnit.to(cp, "asm-6.0_BETA");

        assertNull(artifact.getClazz("module-info"));
    }
}
