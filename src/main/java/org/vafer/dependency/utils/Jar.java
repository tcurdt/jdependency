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
package org.vafer.dependency.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarInputStream;


public class Jar {

	private final File file;
	private final boolean relocate;

	public Jar( final File pFile, final boolean pRelocate ) throws FileNotFoundException {
		file = pFile;
		relocate = pRelocate;
	}

    /**
     * Converts a String into name that can be used as java package name
     *
     * @param pName input String to create a java package name from
     * @return java package name derived from input String
     */
    private String convertToValidPackageName( final String pName )
    {
        final char[] chars = pName.toCharArray();
        final StringBuffer sb = new StringBuffer();

        if ( chars.length > 0 )
        {
            final char c = chars[0];
            if ( Character.isJavaIdentifierStart( c ) )
            {
                sb.append( c );
            }
            else
            {
                sb.append( "C" );
            }
        }

        for ( int i = 1; i < chars.length; i++ )
        {
            final char c = chars[i];
            if ( Character.isJavaIdentifierPart( c ) )
            {
                sb.append( c );
            }
        }

        return sb.toString();
    }

    public String getRelocatePrefix() {
		return convertToValidPackageName( getFile().getName() ) + '/';    	
    }
	
	public String getPrefix() {
		if (relocate) {
			return getRelocatePrefix();
		}
		return "";
	}
	
	public File getFile() {
		return file;
	}
	
	public JarInputStream getInputStream() throws IOException {
		return new JarInputStream(new FileInputStream(file));
	}

	public String toString() {
		return file.toString();
	}


}