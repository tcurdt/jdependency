/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vafer.dependency;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.vafer.dependency.asm.DependencyVisitor;

public final class ClazzpathUnit
{

    private final File location;

    private final Clazzpath clazzpath;

    private final Map clazzes = new HashMap();

    private final Map dependencies = new HashMap();

    public ClazzpathUnit( final Clazzpath pClazzpath, final String pLocation )
        throws IOException
    {
        clazzpath = pClazzpath;
        clazzpath.units.add( this );
        location = new File(pLocation);

        ZipFile f = new ZipFile( pLocation );

        Enumeration en = f.entries();
        while ( en.hasMoreElements() )
        {
            ZipEntry entry = (ZipEntry) en.nextElement();
            String entryName = entry.getName();
            if ( entryName.endsWith( ".class" ) )
            {

                final String clazzName = entryName.substring( 0, entryName.length() - 6 ).replace( '/', '.' );

                Clazz clazz = clazzpath.getClazz( clazzName );

                if ( clazz == null )
                {
                    clazz = (Clazz) clazzpath.missing.get( clazzName );

                    if ( clazz != null )
                    {
                        // already marked missing
                        clazz = (Clazz) clazzpath.missing.remove( clazzName );
                    }
                    else
                    {
                        clazz = new Clazz( clazzName );
                    }
                }
                else
                {
                    // classpath clash                     
                }

                clazzes.put( clazzName, clazz );
                clazzpath.clazzes.put( clazzName, clazz );

                final DependencyVisitor v = new DependencyVisitor();
                new ClassReader( f.getInputStream( entry ) ).accept( v, false );
                final Set depNames = v.getDependencies();

                for ( final Iterator it = depNames.iterator(); it.hasNext(); )
                {
                    String depName = (String) it.next();

                    Clazz dep = clazzpath.getClazz( depName );

                    if ( dep == null )
                    {
                        // there is no such clazz yet
                        dep = (Clazz) clazzpath.missing.get( depName );
                    }

                    if ( dep == null )
                    {
                        // it is also not recorded to be missing
                        dep = new Clazz( depName );
                        clazzpath.missing.put( depName, dep );
                    }

                    if ( dep != clazz )
                    {
                        dependencies.put( depName, dep );
                        clazz.addDependency( dep );
                    }
                }
            }
        }

        f.close();
    }

    public File getFile()
    {
    	return location;
    }
    
    public Set getClazzes()
    {
        final Set all = new HashSet();
        for ( final Iterator it = clazzes.values().iterator(); it.hasNext(); )
        {
            final Clazz clazz = (Clazz) it.next();
            all.add(clazz);
        }
        return all;
    }

    public Clazz getClazz( final String pClazzName )
    {
        return (Clazz) clazzes.get( pClazzName );
    }

    public Set getDependencies()
    {
        final Set all = new HashSet();
        for ( final Iterator it = dependencies.values().iterator(); it.hasNext(); )
        {
            final Clazz clazz = (Clazz) it.next();
            all.add(clazz);
        }
        return all;
    }

    public Set getTransitiveDependencies()
    {
        final Set all = new HashSet();
        for ( final Iterator it = clazzes.values().iterator(); it.hasNext(); )
        {
            final Clazz clazz = (Clazz) it.next();
            clazz.findTransitiveDependencies( all );
        }
        return all;
    }

    /*
    public void write( final File pDir, final Matcher pMatcher, final Console pConsole )
        throws IOException
    {

        final String name = location.getName();
        final String nameJar = name.substring( 0, name.lastIndexOf( '.' ) ) + "-minimal.jar";

        final File in = location;
        final File out = new File( pDir, nameJar );
        out.delete();

        rewriteJar( new JarInputStream( new FileInputStream( in ) ), pMatcher,
                    new JarOutputStream( new FileOutputStream( out ) ) );

        final long inLength = in.length();
        final long outLength = out.length();

        if ( outLength == 0 )
        {
            if ( pConsole != null )
            {
                pConsole.println( name + " is empty! Dependency can be removed!" );
            }
            return;
        }

        if ( pConsole != null )
        {
            final DecimalFormat format = new DecimalFormat( "##0.0" );
            pConsole.println( name + " classes:" + clazzes.size() + " dependencies:" + dependencies.size() + " => "
                + format.format( 100 * outLength / inLength ) + "% (" + inLength + "->" + outLength + " bytes)" );
        }
    }
    */

    /*
    private static boolean rewriteJar( final JarInputStream pInput, final Matcher pMatcher,
                                       final JarOutputStream pOutput )
        throws IOException
    {

        boolean changed = false;

        while ( true )
        {
            final JarEntry entry = pInput.getNextJarEntry();

            if ( entry == null )
            {
                break;
            }

            if ( entry.isDirectory() )
            {
                pOutput.putNextEntry( new JarEntry( entry ) );
                continue;
            }

            final String name = entry.getName();

            if ( name.endsWith( ".class" ) )
            {
                if ( pMatcher.isMatching( name ) )
                {

                    pOutput.putNextEntry( new JarEntry( name ) );
                    IOUtils.copy( pInput, pOutput );

                    continue;
                }

                IOUtils.copy( pInput, new NullOutputStream() );

                changed = true;

            }
            else if ( name.endsWith( ".jar" ) || name.endsWith( ".ear" ) || name.endsWith( ".zip" )
                || name.endsWith( ".war" ) )
            {

                pOutput.putNextEntry( new JarEntry( name ) );
                IOUtils.copy( pInput, pOutput );

            }
            else
            {
                pOutput.putNextEntry( new JarEntry( name ) );
                IOUtils.copy( pInput, pOutput );
            }
        }

        pInput.close();
        pOutput.close();

        return changed;
    }
    */

}
