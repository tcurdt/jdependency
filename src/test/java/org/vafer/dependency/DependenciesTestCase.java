package org.vafer.dependency;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.vafer.dependency.classes.Class1;
import org.vafer.dependency.classes.Class2;
import org.vafer.dependency.classes.Class3;
import org.vafer.dependency.utils.DependencyUtils;

public class DependenciesTestCase extends TestCase {

	public void testClass1() throws Exception {
		final Set dependencies = DependencyUtils.getDependenciesOfClass(Class1.class);
		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
				"java.lang.Object"
				}));
		assertEquals(dependencies, expectedDependencies);
	}

	public void testClass2() throws Exception {
		final Set dependencies = DependencyUtils.getDependenciesOfClass(Class2.class);
		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
				"java.lang.String",
				"java.lang.Object"
				}));
		assertEquals(dependencies, expectedDependencies);
	}

	public void testClass3() throws Exception {
		final Set dependencies = DependencyUtils.getDependenciesOfClass(Class3.class);
		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
				"java.lang.String",
				"java.lang.Object"
				}));
		assertEquals(dependencies, expectedDependencies);
	}
}
