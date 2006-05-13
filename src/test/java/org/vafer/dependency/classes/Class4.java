package org.vafer.dependency.classes;

import java.util.HashMap;
import java.util.Map;

public final class Class4 {
	
	private String s;
	
	public Class4( final String pString ) {
		final Map m = new HashMap();
		m.put(pString, pString);
		s = pString;
		m.put(s, s);
	}
	
}
