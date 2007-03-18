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
import java.io.InputStream;
import java.util.jar.JarInputStream;


public final class JarProcessor implements ResourceRenamer, ResourceMatcher {

	private final InputStream inputStream;
	private final String name;
	private final String prefix;

	private ResourceMatcher matcher = new AllResouceMatcher();
	private ResourceRenamer renamer = new NoResourceRenamer();
	private boolean relocate;

	public JarProcessor( final File pFile ) throws FileNotFoundException {
		this(new FileInputStream(pFile), pFile.getName());
	}

	public JarProcessor( final InputStream pInputStream, final String pName ) {
		inputStream = pInputStream;
		name = pName;
		prefix = name.substring(0,4) + "/"; 
	}

	public JarProcessor setRelocate( final boolean pRelocate ) {
		relocate = pRelocate;
		return this;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public String getNewNameFor(String pOldName) {
		if (relocate) {
			return getPrefix() + renamer.getNewNameFor(pOldName);				
		}
		return renamer.getNewNameFor(pOldName);
	}

	public boolean keepResourceWithName(String pOldName) {
		return matcher.keepResourceWithName(pOldName);
	}

	public JarInputStream getInputStream() throws IOException {
		return new JarInputStream(inputStream);
	}


	public String toString() {
		return name.toString();
	}

}