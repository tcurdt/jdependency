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
import java.util.jar.JarOutputStream;

import org.apache.commons.io.IOUtils;
import org.vafer.dependency.resources.Version;


public class Jar {

	private final File file;

	public Jar( final File pFile ) throws FileNotFoundException {
		file = pFile;
	}

	public File getFile() {
		return file;
	}
	
	public JarInputStream getInputStream() throws IOException {
		return new JarInputStream(new FileInputStream(file));
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

	public void onStart(JarOutputStream os) throws IOException {
	}

	public void onResource(String name, InputStream is, JarOutputStream os) throws IOException {
		IOUtils.copy(is, os);
	}

	public void onStop(JarOutputStream os) throws IOException {
	}

	public String toString() {
		return file.toString();
	}


}