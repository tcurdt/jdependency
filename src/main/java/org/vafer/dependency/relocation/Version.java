/**
 * 
 */
package org.vafer.dependency.relocation;


public final class Version {
	private final Jar jar;
	private final byte[] digestBytes;

	public Version( final Jar pJar, final byte[] pDigestBytes ) {
		jar = pJar;
		digestBytes = pDigestBytes;
	}
	
	public Jar getJar() {
		return jar;
	}
	
	public byte[] getDigestBytes() {
		return digestBytes;
	}
}