package org.vafer.jdependency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ClazzpathTestCase {
    @Test
    public void testShouldAddClasses() throws IOException {
        
        final InputStream jar1 = getClass().getClassLoader().getResourceAsStream("jar1.jar");
        final InputStream jar2 = getClass().getClassLoader().getResourceAsStream("jar2.jar");

        assertNotNull(jar1);
        assertNotNull(jar2);
        
        final Clazzpath cp = new Clazzpath();
        cp.addClazzpathUnit(jar1, "jar1.jar");
        cp.addClazzpathUnit(jar2, "jar2.jar");
        
        final ClazzpathUnit[] units = cp.getUnits();        
        assertEquals(2, units.length);
        
        assertEquals(129, cp.getClazzes().size());      
    }

    @Test
    public void testShouldRemoveClasspathUnit() throws IOException {

        final InputStream jar1 = getClass().getClassLoader().getResourceAsStream("jar1.jar");
        final InputStream jar2 = getClass().getClassLoader().getResourceAsStream("jar2.jar");

        assertNotNull(jar1);
        assertNotNull(jar2);
        
        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit unit1 = cp.addClazzpathUnit(jar1, "jar1.jar");
        
        assertEquals(59, cp.getClazzes().size());       
        
        final ClazzpathUnit unit2 = cp.addClazzpathUnit(jar2, "jar2.jar");
                
        assertEquals(129, cp.getClazzes().size());
        
        cp.removeClazzpathUnit(unit1);
        
        assertEquals(70, cp.getClazzes().size());

        cp.removeClazzpathUnit(unit2);
        
        assertEquals(0, cp.getClazzes().size());
    }
    
    @Test
    public void testShouldRevealMissingClasses() throws IOException {

        final InputStream jar1 = getClass().getClassLoader().getResourceAsStream("jar1-missing.jar");

        assertNotNull(jar1);
        
        final Clazzpath cp = new Clazzpath();
        cp.addClazzpathUnit(jar1, "jar1-missing.jar");
        
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

        final InputStream jar1 = getClass().getClassLoader().getResourceAsStream("jar1.jar");
        final InputStream jar2 = getClass().getClassLoader().getResourceAsStream("jar1.jar");

        assertNotNull(jar1);
        assertNotNull(jar2);
        
        final Clazzpath cp = new Clazzpath();
        cp.addClazzpathUnit(jar1, "jar1.jar");
        cp.addClazzpathUnit(jar2, "jar2.jar");
        
        final Set<Clazz> actual = cp.getClashedClazzes();       
        final Set<Clazz> expected = cp.getClazzes(); 

        assertEquals(expected, actual);
    }
    
    @Test
    public void testShouldFindUnusedClasses() throws IOException {

        final InputStream jar3using1 = getClass().getClassLoader().getResourceAsStream("jar3using1.jar");
        final InputStream jar1 = getClass().getClassLoader().getResourceAsStream("jar1.jar");

        assertNotNull(jar3using1);
        assertNotNull(jar1);

        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit artifact = cp.addClazzpathUnit(jar3using1, "jar3using1.jar");
        cp.addClazzpathUnit(jar1, "jar1.jar");

        final Set<Clazz> removed = cp.getClazzes();        
        removed.removeAll(artifact.getClazzes());
        removed.removeAll(artifact.getTransitiveDependencies());
        
        assertEquals("" + removed, 56, removed.size());

        final Set<Clazz> kept = cp.getClazzes();
        kept.removeAll(removed);

        assertEquals("" + kept, 4, kept.size());        
    }
}
