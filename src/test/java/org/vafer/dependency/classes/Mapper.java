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
package org.vafer.dependency.classes;

import java.util.HashMap;
import java.util.Map;

public class Mapper {

	private final static Map map;
	
	static {
		map = new HashMap();
		map.put("a", "m/a");
		map.put("b", "m/b");		
		map.put("b", "m/b");		
	}
	
	public final static String resolve( final String pResourceName ) {
		
		final String newResourceName = (String) map.get(pResourceName);
		
		if (newResourceName == null) {
			return pResourceName;
		}
				
		return newResourceName;
	}

}
