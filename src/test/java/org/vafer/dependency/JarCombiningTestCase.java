package org.vafer.dependency;

import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import org.vafer.dependency.utils.JarUtils;
import org.vafer.dependency.utils.ResourceMatcher;
import org.vafer.dependency.utils.ResourceRenamer;

public class JarCombiningTestCase extends TestCase {

	private final class JarResourceRenamer implements ResourceRenamer {
		private final File file;
		
		public JarResourceRenamer( final File pFile ) {
			file = pFile;
		}
		
		public String getNewNameFor( final String pOldName ) {
			if (pOldName.startsWith("java")) {
				return pOldName;
			}
			return getPrefixFrom(file) + "/" + pOldName;
		}
		
		public String getPrefixFrom( final File pFile ) {
			return pFile.getName().substring(0, 4);
		}
	}
	
	public void testMergeWithRelocate() throws Exception {
		
		final URL jar1jar = this.getClass().getClassLoader().getResource("jar1.jar");		
		final URL jar2jar = this.getClass().getClassLoader().getResource("jar2.jar");

		assertNotNull(jar1jar);
		assertNotNull(jar2jar);
		
		final ResourceMatcher matcher = new ResourceMatcher() {
			public boolean keepResourceWithName(String pOldName) {
				return true;
			}
		};

		JarUtils.combineJars(
				new File[] {
					new File(jar1jar.toURI()),
					new File(jar2jar.toURI())
				},
				new ResourceMatcher[] {
					matcher,
					matcher
				},
				new ResourceRenamer[] {
					new JarResourceRenamer(new File(jar1jar.toURI())),
					new JarResourceRenamer(new File(jar2jar.toURI()))
				},
				new File("out.jar"),
				new Console() {
					public void println(String pString) {
						System.out.println(pString);
					}
				}
		);
	}

	public void testMergeWithoutRelocate() throws Exception {
		
	}

}
