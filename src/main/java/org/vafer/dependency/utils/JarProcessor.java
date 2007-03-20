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
import java.io.OutputStream;
import java.util.jar.JarInputStream;

import org.apache.commons.io.IOUtils;


public class JarProcessor implements ResourceHandler {

	private final File file;

	public JarProcessor( final File pFile ) throws FileNotFoundException {
		file = pFile;
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

	public String getNewNameFor(String name) {
		return name;
	}

	public boolean keepResource(String name) {
		return true;
	}

	public Version pickVersion(Version[] versions) {
		return versions[0];
	}

	public void copy(String name, InputStream is, OutputStream os) throws IOException {
		IOUtils.copy(is, os);
	}

}