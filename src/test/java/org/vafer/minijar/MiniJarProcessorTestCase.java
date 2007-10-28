package org.vafer.minijar;

import java.io.File;

import junit.framework.TestCase;

import org.vafer.jar.Jar;
import org.vafer.minijar.MiniJarProcessor.Console;
import org.vafer.minijar.MiniJarProcessor.JarFilter;

public final class MiniJarProcessorTestCase extends TestCase {

	public void testMiniJar() throws Exception {
		
	}
	
	public void testUeberJar() throws Exception {
		final MiniJarProcessor proc = new MiniJarProcessor(new Console() {
			public void error(String error) {
				System.err.println(error);
			}
			public void info(String message) {
				System.out.println(message);
			}			
		});
		
		final Jar project = new Jar(new File("/Users/tcurdt/.m2/repository/org/vafer/jdeb/0.4/jdeb-0.4.jar"));
		final Jar dependency = new Jar(new File("/Users/tcurdt/.m2/repository/org/bouncycastle/bcpg-jdk12/130/bcpg-jdk12-130.jar")); 
		
		final JarFilter all = new JarFilter() {
			public boolean accept(Jar jar) {
				return true;
			}			
		};
		
		proc.ueberjar( project, new Jar[] { dependency }, all, all);
	}
}
