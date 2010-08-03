package org.vafer.jdependency;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class ClazzpathTestCase extends TestCase {
	
	public void testShouldAddClasses() throws IOException {
		
		final InputStream jar1 = null;		
		final InputStream jar2 = null;
		
		final Clazzpath cp = new Clazzpath();
		cp.addClazzpathUnit(jar1, "jar1.jar");
		cp.addClazzpathUnit(jar2, "jar2.jar");
		
		final ClazzpathUnit[] units = cp.getUnits();		
		assertEquals(2, units.length);
		
		final Set clazzes = cp.getClazzes();
		assertEquals(0, clazzes.size());
		
		final Set missing = cp.getMissingClazzes();
		assertEquals(0, missing.size());		
	}

	public void testShouldRevealMissingClasses() throws IOException {

		final InputStream jar1 = null;		
		final InputStream jar2 = null;
		
		final Clazzpath cp = new Clazzpath();
		cp.addClazzpathUnit(jar1, "jar1.jar");
		cp.addClazzpathUnit(jar2, "jar2.jar");
		
		final Set missing = cp.getMissingClazzes();
		assertEquals(0, missing.size());				
	}
	
	public void testShouldShowClasspathUnitsResponsibleForClash() throws IOException {
		final InputStream jar1 = null;		
		final InputStream jar2 = null;
		
		final Clazzpath cp = new Clazzpath();
		cp.addClazzpathUnit(jar1, "jar1.jar");
		cp.addClazzpathUnit(jar2, "jar2.jar");
		
		final Set clashed = cp.getClashedClazzes();
		final Set expectedClashed = new HashSet(Arrays.asList(new String[] {
				"java.lang.String",
				}));
		assertEquals(expectedClashed, clashed);
	}

}
