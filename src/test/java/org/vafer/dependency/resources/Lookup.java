/**
 * 
 */
package org.vafer.dependency.resources;

public class Lookup {
	public void find() {
		this.getClass().getClassLoader().getResourceAsStream("org/vafer/dependency/resources/Lookup.class");
	}
}