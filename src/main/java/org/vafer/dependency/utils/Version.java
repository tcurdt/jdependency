/**
 * 
 */
package org.vafer.dependency.utils;

public final class Version {
	final JarProcessor jar;
	private final byte[] digestBytes;

	public Version( final JarProcessor pJar, final byte[] pDigestBytes ) {
		jar = pJar;
		digestBytes = pDigestBytes;
	}
}