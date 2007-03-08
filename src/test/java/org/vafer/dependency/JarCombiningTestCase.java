package org.vafer.dependency;

import java.io.File;

import junit.framework.TestCase;

import org.vafer.dependency.utils.JarUtils;
import org.vafer.dependency.utils.ResourceMatcher;
import org.vafer.dependency.utils.ResourceRenamer;

public class JarCombiningTestCase extends TestCase {

	public void testMergeWithRelocate() throws Exception {
		JarUtils.combineJars(
				new File[] {
					new File("/Users/tcurdt/.m2/repository/commons-io/commons-io/1.2/commons-io-1.2.jar")
				},
				new ResourceMatcher[] { new ResourceMatcher() {
					public boolean keepResourceWithName(String pOldName) {
						return true;
					}

				}},
				new ResourceRenamer[] { new ResourceRenamer() {
					public String getNewNameFor(String pOldName) {
						return "relocated/" + pOldName;
					}
				}},
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
