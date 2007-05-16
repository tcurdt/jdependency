package org.vafer.dependency;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;
import org.vafer.dependency.Clazzpath.ClashHandler;

public final class WarTestCase extends TestCase {

	
	public void testWar() throws IOException {
		
		final Clazzpath clazzpath = new Clazzpath();
		
		final ClashHandler handler = new ClashHandler() {
			public void handleClash( final Clazz pClazz ) {
				System.out.println("class " + pClazz + " already loaded from " + pClazz.getClazzpathUnit());
			}
		};
		
		final JarInputStream inputStream = new JarInputStream(new FileInputStream("/Volumes/Venice/os/backend/staging/app/www/cow-editors.war"));

		while (true) {
            final JarEntry entry = inputStream.getNextJarEntry();
            
            if (entry == null) {
                break;
            }

            final String entryName = entry.getName();

            if (!entryName.endsWith(".jar")) {
            	IOUtils.copy(inputStream, new NullOutputStream());
            	continue;
            }
            
			System.out.println("adding jar " + entryName + " to classpath");

			clazzpath.addClazzpathUnit(inputStream, entryName, handler);
        }
		
        inputStream.close();
	}
}
