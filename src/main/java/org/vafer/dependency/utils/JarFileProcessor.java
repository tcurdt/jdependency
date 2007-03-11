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
import java.io.IOException;
import java.util.jar.JarInputStream;


public final class JarFileProcessor implements JarProcessor {
	
	private final File file;
	private final ResourceMatcher matcher;
	private final ResourceRenamer renamer;
	private final boolean relocate;
	
	public JarFileProcessor( final File pFile ) {
		this(pFile, true);
	}

	public JarFileProcessor( final File pFile, final boolean pRelocate ) {
		this(pFile, pRelocate, new AllResouceMatcher(), new NoResourceRenamer());
	}

	public JarFileProcessor( final File pFile, final boolean pRelocate, final ResourceMatcher pMatcher, final ResourceRenamer pRenamer) {
		file = pFile;
		relocate = pRelocate;
		matcher = pMatcher;
		renamer = pRenamer;
	}

	public String getPrefix() {
		return file.getName().substring(0,4) + "/";
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
		return new JarInputStream(new FileInputStream(file));
	}


	public String toString() {
		return file.toString();
	}
	
}