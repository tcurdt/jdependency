/**
 * 
 */
package org.vafer.dependency.resources;

import java.io.InputStream;
import java.net.URL;

public class Lookup {
	public InputStream find() {
		URL url = getClass().getResource("something");
		
		return this.getClass().getResourceAsStream(this.getClass().getName());
	}
}