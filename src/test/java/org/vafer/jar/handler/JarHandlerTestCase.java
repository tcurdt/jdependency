package org.vafer.jar.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.JarEntry;

import junit.framework.TestCase;

import org.vafer.jar.AbstractJarTestCase;
import org.vafer.jar.Jar;
import org.vafer.jar.JarProcessor;
import org.vafer.jar.handler.FilteringJarHandler.JarEntryFilter;

public class JarHandlerTestCase extends AbstractJarTestCase {

	
	private File output;
	
	protected void setUp() throws Exception {
		output = File.createTempFile("output", "jar");
	}

	protected void tearDown() throws Exception {
		output.delete();
	}


	private VersionsJarHandler merge( final String[] resources1, final String[] resources2, final JarHandler handler ) throws Exception {
		final File jar1 = createJar(this.getClass(), resources1);
		final File jar2 = createJar(this.getClass(), resources2);


		new JarProcessor().processJars(new Jar[] { new Jar(jar1), new Jar(jar2) }, handler);
		
		final VersionsJarHandler verify = new VersionsJarHandler();
		new JarProcessor().processJars(new Jar[] { new Jar(output) }, verify);

		return verify;
	}
	
	
	public void testCopying() throws Exception {
		
		final VersionsJarHandler verify = merge(
				new String[] { classToResource(JarHandlerTestCase.class.getName()) },
				new String[] { classToResource(TestCase.class.getName()) },
				new OutputJarHandler(new FileOutputStream(output))
				);
				
		assertEquals(2, verify.size());		
	}
	
	public void testFiltering() throws Exception {

		{
		final VersionsJarHandler verify = merge(
				new String[] { classToResource(JarHandlerTestCase.class.getName()) },
				new String[] { classToResource(TestCase.class.getName()) },
				new FilteringJarHandler( new JarEntryFilter() {
					public boolean accept(JarEntry entry) {
						return true;
					}				
				}).chain(new OutputJarHandler(new FileOutputStream(output))));

		assertEquals(2, verify.size());
		}

		{
		final VersionsJarHandler verify = merge(
				new String[] { classToResource(JarHandlerTestCase.class.getName()) },
				new String[] { classToResource(TestCase.class.getName()) },
				new FilteringJarHandler( new JarEntryFilter() {
					public boolean accept(JarEntry entry) {
						return entry.getName().startsWith("junit");
					}				
				}).chain(new OutputJarHandler(new FileOutputStream(output))));

		assertEquals(1, verify.size());
		}

	}

	public void testRenaming() throws Exception {
		{
			final VersionsJarHandler verify = merge(
					new String[] { classToResource(JarHandlerTestCase.class.getName()) },
					new String[] { classToResource(TestCase.class.getName()) },
					new RenamingJarHandler( new RenamingJarHandler.Mapper() {
						public String getNameFor(Jar jar, String name) {
							return "prefix/" + name;
						}			
					}).chain(new OutputJarHandler(new FileOutputStream(output))));

			assertEquals(2, verify.size());
		}
	}

	public void testChaining() throws Exception {
		{
			final VersionsJarHandler verify = merge(
					new String[] { classToResource(JarHandlerTestCase.class.getName()) },
					new String[] { classToResource(TestCase.class.getName()) },

					new FilteringJarHandler( new JarEntryFilter() {
						public boolean accept(JarEntry entry) {
							return entry.getName().startsWith("junit");
						}				
					}).chain(
					new RenamingJarHandler( new RenamingJarHandler.Mapper() {
						public String getNameFor(Jar jar, String name) {
							return "prefix/" + name;
						}			
					}).chain(
					new OutputJarHandler(new FileOutputStream(output)))));

			assertEquals(1, verify.size());
			assertTrue(verify.getVersions("prefix/" + classToResource(TestCase.class.getName())).length == 1);
		}
	}
	
}
