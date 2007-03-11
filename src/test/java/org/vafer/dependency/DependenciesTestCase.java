/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.vafer.dependency;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.vafer.dependency.classes.Class1;
import org.vafer.dependency.classes.Class2;
import org.vafer.dependency.classes.Class3;
import org.vafer.dependency.classes.Class4;
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

	public void testClass4() throws Exception {
		final Set dependencies = DependencyUtils.getDependenciesOfClass(Class4.class);
		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
				Class4.class.getName(),
				"java.util.HashMap",
				"java.util.Map",
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
