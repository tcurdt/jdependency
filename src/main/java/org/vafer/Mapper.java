package org.vafer;

import java.util.HashMap;
import java.util.Map;

public class Mapper {

	private final static Map map;
	
	static {
		map = new HashMap();
		map.put("a", "m/a");
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
