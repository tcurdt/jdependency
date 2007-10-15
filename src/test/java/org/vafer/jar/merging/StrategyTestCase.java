package org.vafer.jar.merging;

import java.io.File;
import java.io.FileOutputStream;

import junit.framework.TestCase;

import org.vafer.jar.AbstractJarTestCase;
import org.vafer.jar.Jar;
import org.vafer.jar.JarProcessor;
import org.vafer.jar.handler.VersionsJarHandler;

public final class StrategyTestCase extends AbstractJarTestCase {

	private File output;
	
	protected void setUp() throws Exception {
		output = File.createTempFile("output", "jar");
	}

	protected void tearDown() throws Exception {
		output.delete();
	}


	private VersionsJarHandler merge( final String[] resources1, final String[] resources2, final MergeStrategy strategy ) throws Exception {
		final File jar1 = createJar(this.getClass(), resources1);
		final File jar2 = createJar(this.getClass(), resources2);


		new JarMerger().merge(new Jar[] { new Jar(jar1), new Jar(jar2) }, null, strategy, new FileOutputStream(output));
		
		final VersionsJarHandler verify = new VersionsJarHandler();
		new JarProcessor().processJars(new Jar[] { new Jar(output) }, verify);

		return verify;
	}
	
	
	
	public void testPickFirstStrategy() throws Exception {

		final VersionsJarHandler verify =
			merge( new String[] {
						classToResource(StrategyTestCase.class.getName())
						},
			       new String[] {
						classToResource(StrategyTestCase.class.getName()),
						classToResource(TestCase.class.getName())
						},
					new PickFirstStrategy());
		
		assertEquals(2, verify.size());
	}

	public void testMergeSameStrategy() throws Exception {

		final VersionsJarHandler verify =
			merge( new String[] {
						classToResource(StrategyTestCase.class.getName())
						},
			       new String[] {
						classToResource(StrategyTestCase.class.getName()),
						classToResource(TestCase.class.getName())
						},
					new MergeSameStrategy());
		
		assertEquals(2, verify.size());
	}

}
