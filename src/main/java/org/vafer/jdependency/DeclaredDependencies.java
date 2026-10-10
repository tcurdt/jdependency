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

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Extracts class references that are declared in resources rather than in bytecode
 * (like `META-INF/services` files) so classes that are only loaded reflectively
 * still show up in the dependency graph.
 *
 * Each declaration is an edge from a trigger class to a target class:
 * if the trigger is reachable, the target needs to be kept as well.
 */
public interface DeclaredDependencies {
  /**
   * @param pResourceName the resource path inside the unit, always using '/' as separator
   * @return true if this extractor wants to handle the resource
   */
  boolean accepts(String pResourceName);

  /**
   * Report all declared edges of the resource. Must not close the stream.
   *
   * @param pResourceName the resource path inside the unit, always using '/' as separator
   * @param pInputStream the content of the resource
   * @param pDependency receives (trigger class name, target class name)
   */
  void extract(
    String pResourceName,
    InputStream pInputStream,
    BiConsumer<String, String> pDependency
  ) throws IOException;

  /**
   * A resource `prefix + TRIGGER + suffix` listing one class name per line, `#` starts a comment.
   */
  static DeclaredDependencies classList(
    final String pPrefix,
    final String pSuffix
  ) {
    return new Declarations.ClassList(pPrefix, pSuffix);
  }

  /**
   * `META-INF/services/X` as used by `java.util.ServiceLoader`
   */
  static DeclaredDependencies services() {
    return classList("META-INF/services/", "");
  }

  /**
   * `provides X with Y` clauses of `module-info.class` (JPMS)
   */
  static DeclaredDependencies moduleInfo() {
    return new Declarations.ModuleInfo();
  }

  /**
   * `META-INF/spring/X.imports` as used by Spring Boot 2.7+ auto-configuration
   */
  static DeclaredDependencies springImports() {
    return classList("META-INF/spring/", ".imports");
  }

  /**
   * `META-INF/spring.factories` as used by `SpringFactoriesLoader`
   */
  static DeclaredDependencies springFactories() {
    return new Declarations.SpringFactories();
  }

  /**
   * `META-INF/sisu/javax.inject.Named` index as used by Eclipse Sisu
   */
  static DeclaredDependencies sisuIndex() {
    return classList("META-INF/sisu/", "");
  }

  static List<DeclaredDependencies> all() {
    return Arrays.asList(
      services(),
      moduleInfo(),
      springImports(),
      springFactories(),
      sisuIndex()
    );
  }
}
