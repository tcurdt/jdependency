package org.vafer.dependency.classes;

import java.io.InputStream;

public final class Resolve1 implements Runnable {

	public void run() {		
		try {
			final InputStream is = this.getClass().getResourceAsStream("resource1");
			if (is == null) {
				throw new RuntimeException("could not find resource");
			}
		} catch(Exception e) {			
		}		
	}
}
