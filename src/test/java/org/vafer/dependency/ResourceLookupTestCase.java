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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.vafer.dependency.runtime.MapperDump;

public class ResourceLookupTestCase extends TestCase {

	private static final class BytecodeClassLoader extends ClassLoader {

	    public BytecodeClassLoader() {
			super();
		}

		public BytecodeClassLoader(ClassLoader parent) {
			super(parent);
		}

		public Class loadClass( final byte[] bytecode ) {

			final Class clazz = defineClass(null, bytecode, 0, bytecode.length);
	        
	        return clazz;
	    }
	}

	public void testLookup() throws Exception {
		
		final Map mapping = new HashMap();
		mapping.put("resource", "prefix/resource");
		
		final byte[] generatedClassBytes = MapperDump.dump("org/vafer/dependency/Mapper", mapping);
		
		final BytecodeClassLoader cl = new BytecodeClassLoader();
		final Class c = cl.loadClass(generatedClassBytes);
		final Method m = c.getMethod("resolveResource", new Class[] { String.class });
		final Object r = m.invoke(null, new Object[] { "resource" });
		
		assertEquals("prefix/resource", r);
	}	
}
