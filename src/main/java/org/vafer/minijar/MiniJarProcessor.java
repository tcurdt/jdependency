package org.vafer.minijar;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;

import org.vafer.dependency.Clazz;
import org.vafer.dependency.Clazzpath;
import org.vafer.dependency.ClazzpathUnit;
import org.vafer.jar.Jar;
import org.vafer.jar.handler.FilteringJarHandler.JarEntryFilter;
import org.vafer.jar.handler.RenamingJarHandler.Mapper;
import org.vafer.jar.merging.JarMerger;
import org.vafer.jar.merging.MergeStrategy;
import org.vafer.jar.merging.PickFirstStrategy;

public final class MiniJarProcessor {
	
	public interface Console {
		void info( String message );
		void error( String error );
	}
	
	public interface JarFilter {
		boolean accept( final Jar jar );
	}

	
	private final Console console;
	
	public MiniJarProcessor( final Console pConsole ) {
		console = pConsole;
	}
	
	public void minijars( final Jar project, final Jar[] dependencies ) throws IOException {
		
		
		final Clazzpath clazzpath = new Clazzpath();            	
        final ClazzpathUnit jar = clazzpath.addClazzpathUnit(project.getInputStream(), project.getName(), null);

        for (int i = 0; i < dependencies.length; i++) {
			final Jar dependency = dependencies[i];

			clazzpath.addClazzpathUnit(dependency.getInputStream(), dependency.getName(), null);			
		}
        
        final Set removable = clazzpath.getClazzes();
    
        final int total = removable.size();
        
        removable.removeAll(jar.getClazzes());
        removable.removeAll(jar.getTransitiveDependencies());

        console.info("Can remove " + removable.size() + " of " + total + " classes (" + (int) ( 100 * removable.size() / total ) + "%).");

        

		for (int i = 0; i < dependencies.length; i++) {
	        final Jar dependency = dependencies[i];

			final MergeStrategy strategy = new PickFirstStrategy();
	    	
	    	final JarMerger merger = new JarMerger(
	    		new JarEntryFilter() {
					public boolean accept( final JarEntry entry ) {
						if (removable.contains(new Clazz (resourceToClass(entry.getName())))) {
							console.info( "removing unused " + entry.getName());
							return false;
						}
						return true;
					}	    		
	    		}, null);
	    	
	    	merger.merge(new Jar[] { dependency }, null, strategy, null);			
		}
	}
	
	public void ueberjar( final Jar project, final Jar[] dependencies, final JarFilter dependenciesToMergeFilter, final JarFilter dependenciesToRelocateFilter ) throws IOException {

		final Clazzpath clazzpath = new Clazzpath();            	
        final ClazzpathUnit jar = clazzpath.addClazzpathUnit(project.getInputStream(), project.getName(), null);

        for (int i = 0; i < dependencies.length; i++) {
			final Jar dependency = dependencies[i];

			clazzpath.addClazzpathUnit(dependency.getInputStream(), dependency.getName(), null);			
		}
        
        final Set removable = clazzpath.getClazzes();
    
        final int total = removable.size();
        
        removable.removeAll(jar.getClazzes());
        removable.removeAll(jar.getTransitiveDependencies());

        console.info("Can remove " + removable.size() + " of " + total + " classes (" + (int) ( 100 * removable.size() / total ) + "%).");
		
        
        final Set jarsToMergeSet = new HashSet();
        jarsToMergeSet.add(project);
        for (int i = 0; i < dependencies.length; i++) {
        	if (dependenciesToMergeFilter.accept(dependencies[i])) {
        		jarsToMergeSet.add(dependencies[i]);
        	}
		}
        final Jar[] jarsToMerge = new Jar[jarsToMergeSet.size()];
        jarsToMergeSet.toArray(jarsToMerge);
		
		
		
		final MergeStrategy strategy = new PickFirstStrategy();

    	
    	
    	final JarMerger merger = new JarMerger(
    		new JarEntryFilter() {
				public boolean accept( final JarEntry entry ) {
					if (removable.contains(new Clazz (resourceToClass(entry.getName())))) {
						console.info( "removing unused " + entry.getName());
						return false;
					}
					return true;
				}
    		
    		}, new Mapper() {
				public String getNameFor( final Jar jar, final String name ) {
					
					if (jar == project) {
						return name;
					}
					
					
            		if (dependenciesToRelocateFilter.accept(jar)) {
		            	final String prefix = prefixFromJar(jar);
		            	return prefix + name;
            		}

					return name;
				}        			
    		});
    	
    	merger.merge(jarsToMerge, null, strategy, new FileOutputStream("/Users/tcurdt/Desktop/test.jar"));
		
	}
	
	private static String resourceToClass( final String oldName ) {
		return oldName.replace( '/' , '.' ).substring( 0, oldName.length() - ".class".length());
	}
	
	private static String prefixFromJar( final Jar jar ) {
    	final String name = jar.getName();
    	final char[] chars = name.toCharArray();
    	final StringBuffer sb = new StringBuffer();

    	if ( chars.length > 0 ) {
    		final char c = chars[0];
    		if ( Character.isJavaIdentifierStart( c ) ) {
    			sb.append( c );
    		} else {
    			sb.append( "C" );
    		}
    	}

    	for ( int i = 1; i < chars.length; i++ ) {
    		final char c = chars[i];
    		if (Character.isJavaIdentifierPart(c)) {
    			sb.append( c );
    		}
    	}
    	
    	sb.append("/");

    	return sb.toString();
	}
}
