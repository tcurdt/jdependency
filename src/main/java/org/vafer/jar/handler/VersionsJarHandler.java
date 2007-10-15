package org.vafer.jar.handler;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.vafer.jar.Jar;

public class VersionsJarHandler implements JarHandler {
	
	private final Map mappings = new HashMap();
	private final MessageDigest digest;
	private Jar jar;


	private static final class VersionEntry {
	
		private final String name;
		private Version[] versions = new Version[0];

		public VersionEntry( final String pName ) {
			name = pName;
		}

		public void addVersion( final Version pVersion ) {
			final Version[] newVersions = new Version[versions.length+1];
			System.arraycopy(versions, 0, newVersions, 1, versions.length);
			newVersions[0] = pVersion;
			versions = newVersions;
		}

		public Version[] getVersions() {
			return versions;
		}

		public String getName() {
			return name;
		}

		public String toString() {
			return name;
		}

	}
	
	public static final class Version {

		private final Jar jar;
		private final byte[] digestBytes;
		private static final char[] hex = "0123456789ABCDEF".toCharArray();

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
		
		public String getDigestString() {
			final char[] s = new char[digestBytes.length*2];
			int o = 0;
			for (int i = 0; i < digestBytes.length; i++) {
				s[o++] = hex[digestBytes[i] >> 4 & 0x0f];
				s[o++] = hex[digestBytes[i] & 0x0f];
			}
			return new String(s);
		}

	}

	public VersionsJarHandler() throws NoSuchAlgorithmException {
		digest = MessageDigest.getInstance("MD5");
	}
	
	
	public void onStartProcessing() throws IOException {
	}

	public void onStartJar(Jar pJar) throws IOException {
		jar = pJar;
	}

	private static byte[] calculateDigest( final MessageDigest digest, final InputStream inputStream ) throws IOException {
		digest.reset();
		final DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);                
		IOUtils.copy(digestInputStream, new NullOutputStream());
		return digest.digest();
	}

	public void onResource(JarEntry entry, InputStream input) throws IOException {

		
		final String name = entry.getName();
		
		final byte[] checksum = calculateDigest(digest, input);

		final Version version = new Version(jar, checksum);
		
		VersionEntry versionEntry = (VersionEntry) mappings.get(name);
		if (versionEntry == null) {
			versionEntry = new VersionEntry(name);
			mappings.put(name, versionEntry);
		}
		versionEntry.addVersion(version);
	}

	public void onStopJar(Jar jar) throws IOException {
		jar = null;
	}

	public void onStopProcessing() throws IOException {
	}

	public String[] getNames() {
		final String[] names = new String[mappings.size()];
		mappings.keySet().toArray(names);
		return names;
	}

	public Version[] getVersions( final String name ) {
		final VersionEntry enrty = (VersionEntry) mappings.get(name);
		if (enrty != null) {
			return enrty.getVersions();
		}
		return new Version[0];
	}

	public int size() {
		return mappings.size();
	}
	
//	for (Iterator it = mappings.values().iterator(); it.hasNext();) {
//	final Mapping mapping = (Mapping) it.next();
//	final Version[] versions = mapping.getVersions();
//	final Map uniqueVersions = new HashMap();
//	for (int i = 0; i < versions.length; i++) {
//		final Version version = versions[i];
//		uniqueVersions.put(version.getDigestString(), version);
//	}
//	System.out.println("mapping " + mapping.getOldName() + "->" + mapping.getNewName() + " versions:" + versions.length + " uniques:" + uniqueVersions.size());
	
}
