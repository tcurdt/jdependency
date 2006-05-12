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
				Class1.class.getName(),
				"java.lang.Object"
				}));
		assertEquals(expectedDependencies, dependencies);
	}

	public void testClass2() throws Exception {
		final Set dependencies = DependencyUtils.getDependenciesOfClass(Class2.class);
		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
				Class2.class.getName(),
				"java.lang.String",
				"java.lang.Object"
				}));
		assertEquals(expectedDependencies, dependencies);
	}

	public void testClass3() throws Exception {
		final Set dependencies = DependencyUtils.getDependenciesOfClass(Class3.class);
		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
				Class3.class.getName(),
				"java.lang.String",
				"java.lang.Object"
				}));
		assertEquals(expectedDependencies, dependencies);
	}

//	public void testClassObject() throws Exception {
//		final Set dependencies = DependencyUtils.getDependenciesOfClass(Object.class);
//		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
//				}));
//		assertEquals(expectedDependencies, dependencies);
//	}
//
//	public void testClassString() throws Exception {
//		final Set dependencies = DependencyUtils.getDependenciesOfClass(String.class);
//		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
//				}));
//		assertEquals(expectedDependencies, dependencies);
//	}
//
//	public void testClassHashMap() throws Exception {
//		final Set dependencies = DependencyUtils.getDependenciesOfClass(HashMap.class);
//		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
//				}));
//		assertEquals(expectedDependencies, dependencies);
//	}
}
