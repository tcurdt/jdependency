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
package org.vafer.dependency.relocation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.jar.JarInputStream;


public class Jar {

	private final File file;
	private final String prefix;

	public Jar( final File pFile ) throws FileNotFoundException {
		this(pFile, null);
	}
	
	public Jar( final File pFile, final String pPrefix ) throws FileNotFoundException {
		file = pFile;

		if (pPrefix != null) {
			prefix = pPrefix + "/";
		} else {
			prefix = "";
		}
	}

	public String getPrefix() {
		return prefix;
	}
	
	public JarInputStream getInputStream() throws IOException {
		return new JarInputStream(new FileInputStream(file));
	}

	public String toString() {
		return file.toString();
	}


}