/*
 * Copyright 2010 The Apache Software Foundation.
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.vafer.jdependency.utils.DependencyUtils;

public final class DependencyUtilsTestCase extends TestCase {

	public void testShouldFindDependenciesOfClassObject() throws Exception {
		final Set dependencies = DependencyUtils.getDependenciesOfClass(Object.class);
		final Set expectedDependencies = new HashSet(Arrays.asList(new String[] {
				"java.lang.String",
				"java.lang.IllegalArgumentException",
				"java.lang.CloneNotSupportedException",
				"java.lang.Class",
				"java.lang.InterruptedException",
				"java.lang.Integer",
				"java.lang.Object",
				"java.lang.StringBuilder",
				"java.lang.Throwable"
				}));
		assertEquals(expectedDependencies, dependencies);
	}
}
