/*
 * Copyright 2010-2019 The jdependency developers.
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
package org.vafer.jdependency;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class ClazzpathUnitTestCase {

    private static Path resourcePath( String filename ) {
        return Paths.get(filename);
    }

    // mvn is copying all into the test working dir
    private static File resourceFile( String filename ) {
        return Paths.get(filename).toFile();
    }

    @Test
    public void testIssue47() throws IOException {
        final Clazzpath cp = new Clazzpath();

        final ClazzpathUnit u1 = cp.addClazzpathUnit(resourceFile("cxf-core-3.4.0.jar"));
        final Set<String> u1f = u1.getClazzes().stream()
            .filter( i -> i.getName().contains("W3CSchema") )
            .map( i -> i.getName() )
            .collect(Collectors.toSet());
        final Set<String> u1fe = new HashSet<String>(Arrays.asList(
            ));
        assertEquals(u1fe, u1f);

        final ClazzpathUnit u2 = cp.addClazzpathUnit(resourceFile("woodstox-core-6.2.3.jar"));
        final Set<String> u2f = u2.getClazzes().stream()
            .filter( i -> i.getName().contains("W3CSchema") )
            .map( i -> i.getName() )
            .collect(Collectors.toSet());
        final Set<String> u2fe = new HashSet<String>(Arrays.asList(
            "com.ctc.wstx.msv.W3CSchemaFactory",
            "com.ctc.wstx.osgi.ValidationSchemaFactoryProviderImpl$W3CSchema",
            "com.ctc.wstx.msv.W3CSchema"
            ));
        assertEquals(u2fe, u2f);

        final Set<String> units = cp.getClazzes().stream()
            .filter( i -> i.getName().contains("W3CSchema") )
            .flatMap( i -> i.getClazzpathUnits().stream() )
            .map( i -> i.toString() )
            .collect(Collectors.toSet());
        final Set<String> unitse = new HashSet<String>(Arrays.asList(
            "woodstox-core-6.2.3.jar"
            ));
        assertEquals(unitse, units);
    }

    @Test
    public void testShouldAddClasses() throws IOException {
        final Clazzpath cp = new Clazzpath();

        final ClazzpathUnit u = cp.addClazzpathUnit(resourceFile("jar1.jar"));
        final Set<String> uc = u.getClazzes().stream()
            .map(c -> c.getName())
            .collect(Collectors.toSet());
        final Set<String> uce = new HashSet<String>(Arrays.asList(
            "org.apache.commons.io.filefilter.IOFileFilter",
            "org.apache.commons.io.LineIterator",
            "org.apache.commons.io.output.NullWriter",
            "org.apache.commons.io.filefilter.FileFilterUtils",
            "org.apache.commons.io.FileCleaningTracker$Tracker",
            "org.apache.commons.io.EndianUtils",
            "org.apache.commons.io.filefilter.EmptyFileFilter",
            "org.apache.commons.io.filefilter.NotFileFilter",
            "org.apache.commons.io.filefilter.TrueFileFilter",
            "org.apache.commons.io.filefilter.AgeFileFilter",
            "org.apache.commons.io.CopyUtils",
            "org.apache.commons.io.DirectoryWalker",
            "org.apache.commons.io.filefilter.AbstractFileFilter",
            "org.apache.commons.io.output.ByteArrayOutputStream",
            "org.apache.commons.io.filefilter.ConditionalFileFilter",
            "org.apache.commons.io.HexDump",
            "org.apache.commons.io.input.ProxyReader",
            "org.apache.commons.io.filefilter.FileFileFilter",
            "org.apache.commons.io.input.DemuxInputStream",
            "org.apache.commons.io.output.ProxyOutputStream",
            "org.apache.commons.io.filefilter.DirectoryFileFilter",
            "org.apache.commons.io.filefilter.HiddenFileFilter",
            "org.apache.commons.io.IOUtils",
            "org.apache.commons.io.filefilter.SuffixFileFilter",
            "org.apache.commons.io.output.ProxyWriter",
            "org.apache.commons.io.filefilter.FalseFileFilter",
            "org.apache.commons.io.input.NullInputStream",
            "org.apache.commons.io.filefilter.CanReadFileFilter",
            "org.apache.commons.io.output.DemuxOutputStream",
            "org.apache.commons.io.FilenameUtils",
            "org.apache.commons.io.DirectoryWalker$CancelException",
            "org.apache.commons.io.FileCleaningTracker",
            "org.apache.commons.io.filefilter.DelegateFileFilter",
            "org.apache.commons.io.filefilter.AndFileFilter",
            "org.apache.commons.io.IOCase",
            "org.apache.commons.io.FileDeleteStrategy",
            "org.apache.commons.io.FileSystemUtils",
            "org.apache.commons.io.filefilter.SizeFileFilter",
            "org.apache.commons.io.filefilter.OrFileFilter",
            "org.apache.commons.io.filefilter.NameFileFilter",
            "org.apache.commons.io.output.TeeOutputStream",
            "org.apache.commons.io.output.CountingOutputStream",
            "org.apache.commons.io.input.CountingInputStream",
            "org.apache.commons.io.output.DeferredFileOutputStream",
            "org.apache.commons.io.FileUtils",
            "org.apache.commons.io.FileCleaner",
            "org.apache.commons.io.filefilter.PrefixFileFilter",
            "org.apache.commons.io.FileCleaningTracker$Reaper",
            "org.apache.commons.io.input.SwappedDataInputStream",
            "org.apache.commons.io.input.NullReader",
            "org.apache.commons.io.filefilter.WildcardFilter",
            "org.apache.commons.io.output.NullOutputStream",
            "org.apache.commons.io.FileDeleteStrategy$ForceFileDeleteStrategy",
            "org.apache.commons.io.output.LockableFileWriter",
            "org.apache.commons.io.filefilter.WildcardFileFilter",
            "org.apache.commons.io.input.ProxyInputStream",
            "org.apache.commons.io.output.ThresholdingOutputStream",
            "org.apache.commons.io.input.ClassLoaderObjectInputStream",
            "org.apache.commons.io.filefilter.CanWriteFileFilter"
        ));
        assertEquals(uce, uc);
    }

    @Test
    public void testShouldHaveUnitId() throws IOException {

        final Clazzpath cp = new Clazzpath();

        final ClazzpathUnit u1 = cp.addClazzpathUnit(resourceFile("jar1.jar"));
        assertEquals(u1.toString(), "jar1.jar");

        final ClazzpathUnit u1e = cp.addClazzpathUnit(resourceFile("jar1.jar"), "jar1");
        assertEquals(u1e.toString(), "jar1");

        final ClazzpathUnit u2 = cp.addClazzpathUnit(resourcePath("jar2.jar"));
        assertEquals(u2.toString(), "jar2.jar");

        final ClazzpathUnit u2e = cp.addClazzpathUnit(resourcePath("jar2.jar"), "jar2");
        assertEquals(u2e.toString(), "jar2");
    }

    @Test
    public void testDependencies() throws IOException {

        final Clazzpath cp = new Clazzpath();
        final ClazzpathUnit u = cp.addClazzpathUnit(resourceFile("jar1.jar"));

        final Set<Clazz> deps = u.getDependencies();
        assertEquals(116, deps.size());

        final Set<Clazz> transitiveDeps = u.getTransitiveDependencies();
        assertEquals(116, transitiveDeps.size());

    }

}
