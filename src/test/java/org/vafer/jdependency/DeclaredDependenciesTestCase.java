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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;

public class DeclaredDependenciesTestCase {

  private static Set<String> extract(
    DeclaredDependencies pDeclarations,
    String pName,
    String pContent
  ) throws IOException {
    assertTrue(pName, pDeclarations.accepts(pName));
    final Set<String> edges = new HashSet<>();
    pDeclarations.extract(
      pName,
      new ByteArrayInputStream(pContent.getBytes(UTF_8)),
      (from, to) -> edges.add(from + " -> " + to)
    );
    return edges;
  }

  private static Set<String> set(String... pValues) {
    return new HashSet<>(Arrays.asList(pValues));
  }

  @Test
  public void testServices() throws IOException {
    final DeclaredDependencies services = DeclaredDependencies.services();

    assertEquals(
      set("a.Service -> a.Impl", "a.Service -> a.Other$Inner"),
      extract(
        services,
        "META-INF/services/a.Service",
        "# comment\n" +
          "\n" +
          "  a.Impl  # trailing comment\n" +
          "a.Other$Inner\n"
      )
    );

    assertFalse(services.accepts("META-INF/services/"));
    assertFalse(services.accepts("META-INF/services/nested/a.Service"));
    assertFalse(services.accepts("META-INF/MANIFEST.MF"));
  }

  @Test
  public void testSpringImports() throws IOException {
    final DeclaredDependencies imports = DeclaredDependencies.springImports();

    assertEquals(
      set(
        "org.springframework.boot.autoconfigure.AutoConfiguration -> a.FooAutoConfiguration"
      ),
      extract(
        imports,
        "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports",
        "# comment\n" + "a.FooAutoConfiguration\n"
      )
    );

    assertFalse(imports.accepts("META-INF/spring/aot.factories"));
    assertFalse(imports.accepts("META-INF/spring.factories"));
  }

  @Test
  public void testSpringFactories() throws IOException {
    assertEquals(
      set("a.Key -> a.One", "a.Key -> a.Two", "b.Key -> b.One"),
      extract(
        DeclaredDependencies.springFactories(),
        "META-INF/spring.factories",
        "# comment\n" + "a.Key=a.One,\\\n" + "  a.Two\n" + "b.Key = b.One ,\n"
      )
    );
  }

  @Test
  public void testSisuIndex() throws IOException {
    assertEquals(
      set("javax.inject.Named -> a.Component"),
      extract(
        DeclaredDependencies.sisuIndex(),
        "META-INF/sisu/javax.inject.Named",
        "a.Component\n"
      )
    );
  }

  @Test
  public void testModuleInfoNames() {
    final DeclaredDependencies moduleInfo = DeclaredDependencies.moduleInfo();

    assertTrue(moduleInfo.accepts("module-info.class"));
    assertTrue(moduleInfo.accepts("META-INF/versions/11/module-info.class"));
    assertFalse(moduleInfo.accepts("a/module-info.class"));
    assertFalse(moduleInfo.accepts("package-info.class"));
  }

  @Test
  public void testParseClassFileName() {
    assertNull(Clazz.parseClassFileName("module-info.class"));
    assertNull(
      Clazz.parseClassFileName("META-INF/versions/11/module-info.class")
    );
    assertEquals(
      "a.B",
      Clazz.parseClassFileName("META-INF/versions/11/a/B.class").className
    );
  }
}
