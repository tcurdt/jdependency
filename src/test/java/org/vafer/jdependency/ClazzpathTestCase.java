/*
 * Copyright 2010-2017 The jdependency developers.
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
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private static abstract class AddClazzpathUnit {
        abstract boolean isApplicable( String resourceName );

        final ClazzpathUnit to( Clazzpath clazzpath, String resourceName ) throws IOException {
            return to(clazzpath, resourceName, defaultResourceId(resourceName));
        }

        String defaultResourceId(String resourceName) {
            return resourceName;
        }

        abstract ClazzpathUnit to( Clazzpath clazzpath, String resourceName, String resourceId ) throws IOException;
    }

    /**
     * Return parameterized test data collection:
     * <ol>
     * <li>AddClazzpathUnit for classpath-based jars</li>
     * <li>AddClazzpathUnit for filesystem-based jars</li>
     * <li>AddClazzpathUnit for filesystem-based directories</li>
     * </ol>
     * @return Collection&lt;Object[]&gt;
     */
    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[] { new AddClazzpathUnit() {

            ClazzpathUnit to( Clazzpath toClazzpath, String resourceName, String resourceId ) throws IOException {
                InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream(resourceName + ".jar");
                assertNotNull(resourceAsStream);
                return toClazzpath.addClazzpathUnit(resourceAsStream, resourceId);
            }

            boolean isApplicable( String resourceName ) {
                return getClass().getClassLoader().getResource(resourceName + ".jar") != null;
            }

        }}, new Object[] { new AddClazzpathUnit() {

            ClazzpathUnit to( Clazzpath toClazzpath, String resourceName, String resourceId ) throws IOException {
                return toClazzpath.addClazzpathUnit(new File(resourceName + ".jar"), resourceId);
            }

            boolean isApplicable( String resourceName ) {
                return new File(resourceName + ".jar").exists();
            }

            @Override
            String defaultResourceId( String resourceName ) {
                return new File(resourceName + ".jar").getAbsolutePath();
            }

        }}, new Object[] { new AddClazzpathUnit() {

            ClazzpathUnit to( Clazzpath toClazzpath, String resourceName, String resourceId ) throws IOException {
                return toClazzpath.addClazzpathUnit(new File(resourceName), resourceId);
            }

            boolean isApplicable( String resourceName ) {
                return new File(resourceName).exists();
            }

            @Override
            String defaultResourceId( String resourceName ) {
                return new File(resourceName).getAbsolutePath();
            }

        }});
    }

    private final AddClazzpathUnit addClazzpathUnit;

    public ClazzpathTestCase( AddClazzpathUnit pAddClazzpathUnit ) {
        super();
        this.addClazzpathUnit = pAddClazzpathUnit;
    }

    @Test
    public void testShouldAddClasses() throws IOException {
        assumeTrue(addClazzpathUnit.isApplicable("jar1"));
        assumeTrue(addClazzpathUnit.isApplicable("jar2"));

        final Clazzpath cp = new Clazzpath();
        addClazzpathUnit.to(cp, "jar1");
        addClazzpathUnit.to(cp, "jar2");

        final ClazzpathUnit[] units = cp.getUnits();
        assertEquals(2, units.length);

        assertEquals(129, cp.getClazzes().size());
    }

    @Test
    public void testShouldRemoveClasspathUnit() throws IOException {
        assumeTrue(addClazzpathUnit.isApplicable("jar1"));
        assumeTrue(addClazzpathUnit.isApplicable("jar2"));

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
        assumeTrue(addClazzpathUnit.isApplicable("jar1-missing"));

        final Clazzpath cp = new Clazzpath();
        addClazzpathUnit.to(cp, "jar1-missing");

        final Set<Clazz> missing = cp.getMissingClazzes();

        final Set<String> actual = new HashSet<String>();
        for (Clazz clazz : missing) {
            String name = clazz.getName();
            // ignore the rt.jar
            if (!name.startsWith("java")) {
                actual.add(name);
            }
        }

        final Set<String> expected = new HashSet<String>(Arrays.asList(
                "org.apache.commons.io.output.ProxyOutputStream",
                "org.apache.commons.io.input.ProxyInputStream"
                ));

        assertEquals(expected, actual);
    }

    @Test
    public void testShouldShowClasspathUnitsResponsibleForClash() throws IOException {
        assumeTrue(addClazzpathUnit.isApplicable("jar1"));

        final Clazzpath cp = new Clazzpath();
        addClazzpathUnit.to(cp, "jar1");
        addClazzpathUnit.to(cp, "jar1", "jar2");

        final Set<Clazz> actual = cp.getClashedClazzes();
        final Set<Clazz> expected = cp.getClazzes();

        assertEquals(expected, actual);
    }

    @Test
    public void testShouldFindUnusedClasses() throws IOException {
        assumeTrue(addClazzpathUnit.isApplicable("jar3using1"));
        assumeTrue(addClazzpathUnit.isApplicable("jar1"));

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
        assumeTrue(addClazzpathUnit.isApplicable("asm-6.0_BETA"));

        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit artifact = addClazzpathUnit.to(cp, "asm-6.0_BETA");

        assertNull(artifact.getClazz( "module-info" ));
    }
}
