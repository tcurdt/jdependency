/*
 * Copyright 2010-2026 The jdependency developers.
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

import static org.apache.commons.io.FilenameUtils.normalize;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.vafer.jdependency.Clazz.parseClassFileName;
import static org.vafer.jdependency.utils.StreamUtils.asStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarInputStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import org.apache.commons.io.input.MessageDigestInputStream;
import org.objectweb.asm.ClassReader;
import org.vafer.jdependency.Clazz.ParsedFileName;
import org.vafer.jdependency.asm.DependenciesClassAdapter;

public final class Clazzpath {

  private final Set<ClazzpathUnit> units = new HashSet<>();
  private final Map<String, Clazz> missing = new HashMap<>();
  private final Map<String, Clazz> clazzes = new HashMap<>();
  private final Map<String, Clazz> declaredOnly = new HashMap<>();
  private final boolean versions;
  private final List<DeclaredDependencies> declarations;

  private interface Opener {
    InputStream open() throws IOException;
  }

  private static final class Resource {

    public final String fileName;
    public final String forJava;
    public final String name; // Class name !
    public final DeclaredDependencies declaration;
    private final Opener opener;

    Resource(
      final String pFileName,
      final ParsedFileName pParsedFileName,
      final DeclaredDependencies pDeclaration,
      final Opener pOpener
    ) {
      super();
      this.fileName = pFileName;
      forJava = pParsedFileName != null ? pParsedFileName.forJava : null;
      name = pParsedFileName != null ? pParsedFileName.className : null;
      declaration = pDeclaration;
      opener = pOpener;
    }

    InputStream getInputStream() throws IOException {
      return opener.open();
    }
  }

  /**
   * @return a resource for class files and declarations, null for anything else
   */
  private Resource toResource(final String pFileName, final Opener pOpener) {
    final String unixName = separatorsToUnix(pFileName);
    for (DeclaredDependencies declaration : declarations) {
      if (declaration.accepts(unixName)) {
        return new Resource(unixName, null, declaration, pOpener);
      }
    }

    final ParsedFileName parsedFileName = parseClassFileName(pFileName);
    if (parsedFileName != null) {
      return new Resource(pFileName, parsedFileName, null, pOpener);
    }

    return null;
  }

  public Clazzpath() {
    this(false);
  }

  public Clazzpath(final boolean pVersions) {
    this(pVersions, Collections.emptyList());
  }

  public Clazzpath(
    final boolean pVersions,
    final DeclaredDependencies... pDeclarations
  ) {
    this(pVersions, Arrays.asList(pDeclarations));
  }

  /**
   * @param pVersions calculate digests to detect different versions of a class
   * @param pDeclarations extractors for class references declared in resources (like `META-INF/services`)
   */
  public Clazzpath(
    final boolean pVersions,
    final Collection<? extends DeclaredDependencies> pDeclarations
  ) {
    versions = pVersions;
    declarations = new ArrayList<>(pDeclarations);
  }

  public boolean removeClazzpathUnit(final ClazzpathUnit pUnit) {
    final Set<Clazz> unitClazzes = pUnit.getClazzes();

    for (Clazz clazz : unitClazzes) {
      clazz.removeClazzpathUnit(pUnit);
      if (clazz.getClazzpathUnits().size() == 0) {
        clazzes.remove(clazz.getName());
      }
    }

    return units.remove(pUnit);
  }

  public ClazzpathUnit addClazzpathUnit(final File pFile) throws IOException {
    return addClazzpathUnit(pFile.toPath());
  }

  public ClazzpathUnit addClazzpathUnit(final File pFile, final String pId)
    throws IOException {
    return addClazzpathUnit(pFile.toPath(), pId);
  }

  public ClazzpathUnit addClazzpathUnit(final Path pPath) throws IOException {
    return addClazzpathUnit(pPath, pPath.toString());
  }

  public ClazzpathUnit addClazzpathUnit(final Path pPath, final String pId)
    throws IOException {
    final Path path = pPath.toAbsolutePath();

    if (Files.isRegularFile(path)) {
      return addClazzpathUnit(Files.newInputStream(path), pId);
    } else if (Files.isDirectory(path)) {
      final String prefix = separatorsToUnix(normalize(path.toString() + '/'));

      try (Stream<Path> stream = Files.walk(path)) {
        Iterable<Resource> resources = stream
          .filter(Files::isRegularFile)
          .map(p ->
            toResource(p.toString().substring(prefix.length()), () ->
              Files.newInputStream(p)
            )
          )
          .filter(Objects::nonNull)::iterator;

        return addClazzpathUnit(resources, pId, true);
      }
    }

    throw new IllegalArgumentException("neither file nor directory");
  }

  public ClazzpathUnit addClazzpathUnit(
    final InputStream pInputStream,
    final String pId
  ) throws IOException {
    try (final JarInputStream inputStream = new JarInputStream(pInputStream)) {
      Iterable<Resource> resources = asStream(inputStream)
        .map(ZipEntry::getName)
        .map(name -> toResource(name, () -> inputStream))
        .filter(Objects::nonNull)::iterator;

      return addClazzpathUnit(resources, pId, false);
    }
  }

  private ClazzpathUnit addClazzpathUnit(
    final Iterable<Resource> resources,
    final String pId,
    boolean shouldCloseResourceStream
  ) throws IOException {
    final Map<String, Clazz> unitClazzes = new HashMap<>();
    final Map<String, Clazz> unitDependencies = new HashMap<>();

    final ClazzpathUnit unit = new ClazzpathUnit(
      pId,
      unitClazzes,
      unitDependencies
    );

    for (Resource resource : resources) {
      // extract dependencies of clazz
      InputStream inputStream = resource.getInputStream();
      try {
        if (resource.declaration != null) {
          resource.declaration.extract(
            resource.fileName,
            inputStream,
            this::addDeclaredDependency
          );
          continue;
        }

        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        final MessageDigestInputStream calculatingInputStream =
          MessageDigestInputStream.builder()
            .setInputStream(inputStream)
            .setMessageDigest(digest)
            .get();

        if (versions) {
          inputStream = calculatingInputStream;
        }

        final DependenciesClassAdapter v = new DependenciesClassAdapter();
        new ClassReader(inputStream).accept(
          v,
          ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG
        );

        // get or create clazz
        final String clazzName = resource.name;
        Clazz clazz = getClazz(clazzName);
        if (clazz == null) {
          clazz = missing.get(clazzName);

          if (clazz != null) {
            // already marked missing
            clazz = missing.remove(clazzName);
          } else {
            clazz = declaredOnly.remove(clazzName);
          }

          if (clazz == null) {
            clazz = new Clazz(clazzName);
          }
        }
        clazz.addMultiReleaseFile(unit, resource.forJava, resource.fileName);
        final String d = Base64.getEncoder().encodeToString(digest.digest());
        clazz.addClazzpathUnit(unit, d);

        /// add to classpath
        clazzes.put(clazzName, clazz);

        // add to classpath unit
        unitClazzes.put(clazzName, clazz);

        // iterate through all dependencies
        final Set<String> depNames = v.getDependencies();
        for (String depName : depNames) {
          Clazz dep = getClazz(depName);

          if (dep == null) {
            // there is no such clazz yet
            dep = missing.get(depName);
          }

          if (dep == null) {
            // it is also not recorded to be missing
            dep = declaredOnly.remove(depName);
            if (dep == null) {
              dep = new Clazz(depName);
            }
            // add as missing
            missing.put(depName, dep);
          }

          if (dep != clazz) {
            // unit depends on dep
            unitDependencies.put(depName, dep);
            // clazz depends on dep
            clazz.addDependency(dep);
          }
        }
      } catch (java.security.NoSuchAlgorithmException e) {
        // well, let's pack and go home
      } finally {
        if (shouldCloseResourceStream && inputStream != null) {
          inputStream.close();
        }
      }
    }

    units.add(unit);

    return unit;
  }

  private void addDeclaredDependency(final String pFrom, final String pTo) {
    final Clazz from = getOrCreateDeclared(pFrom);
    final Clazz to = getOrCreateDeclared(pTo);
    if (from != to) {
      from.addDeclaredDependency(to);
    }
  }

  private Clazz getOrCreateDeclared(final String pClazzName) {
    Clazz clazz = getClazz(pClazzName);
    if (clazz == null) {
      clazz = missing.get(pClazzName);
    }
    if (clazz == null) {
      // not added to missing as declarations can name classes
      // that are neither on the classpath nor referenced anywhere
      clazz = declaredOnly.computeIfAbsent(pClazzName, Clazz::new);
    }
    return clazz;
  }

  public Set<Clazz> getClazzes() {
    return new HashSet<>(clazzes.values());
  }

  public Map<String, Clazz> getClazzesMap() {
    return new TreeMap<>(clazzes);
  }

  public Set<Clazz> getClashedClazzes() {
    final Set<Clazz> all = new HashSet<>();
    for (Clazz clazz : clazzes.values()) {
      if (clazz.getClazzpathUnits().size() > 1) {
        all.add(clazz);
      }
    }
    return all;
  }

  public Set<Clazz> getMissingClazzes() {
    return new HashSet<>(missing.values());
  }

  public Clazz getClazz(final String pClazzName) {
    return clazzes.get(pClazzName);
  }

  public ClazzpathUnit[] getUnits() {
    return units.toArray(new ClazzpathUnit[units.size()]);
  }
}
