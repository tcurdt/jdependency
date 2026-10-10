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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 * internal - the built-in {@link DeclaredDependencies}
 */
final class Declarations {

  private Declarations() {}

  static final class ClassList implements DeclaredDependencies {

    private final String prefix;
    private final String suffix;

    ClassList(final String pPrefix, final String pSuffix) {
      prefix = pPrefix;
      suffix = pSuffix;
    }

    private String trigger(final String pResourceName) {
      if (
        !pResourceName.startsWith(prefix) || !pResourceName.endsWith(suffix)
      ) {
        return null;
      }
      final String trigger = pResourceName.substring(
        prefix.length(),
        pResourceName.length() - suffix.length()
      );
      if (trigger.isEmpty() || trigger.contains("/")) {
        return null;
      }
      return trigger;
    }

    public boolean accepts(final String pResourceName) {
      return trigger(pResourceName) != null;
    }

    public void extract(
      final String pResourceName,
      final InputStream pInputStream,
      final BiConsumer<String, String> pDependency
    ) throws IOException {
      final String trigger = trigger(pResourceName);
      // not closing the reader as that would close the underlying stream
      final BufferedReader reader = new BufferedReader(
        new InputStreamReader(pInputStream, UTF_8)
      );
      for (
        String line = reader.readLine();
        line != null;
        line = reader.readLine()
      ) {
        final String className = line.split("#", 2)[0].trim();
        if (!className.isEmpty()) {
          pDependency.accept(trigger, className);
        }
      }
    }
  }

  static final class SpringFactories implements DeclaredDependencies {

    private static final Pattern COMMA = Pattern.compile(",");

    public boolean accepts(final String pResourceName) {
      return "META-INF/spring.factories".equals(pResourceName);
    }

    public void extract(
      final String pResourceName,
      final InputStream pInputStream,
      final BiConsumer<String, String> pDependency
    ) throws IOException {
      final Properties properties = new Properties();
      properties.load(pInputStream);
      for (String key : properties.stringPropertyNames()) {
        for (String value : COMMA.split(properties.getProperty(key))) {
          final String className = value.trim();
          if (!className.isEmpty()) {
            pDependency.accept(key.trim(), className);
          }
        }
      }
    }
  }

  static final class ModuleInfo implements DeclaredDependencies {

    private static final Pattern NAME = Pattern.compile(
      "^(?:META-INF/versions/\\d+/)?module-info\\.class$"
    );

    public boolean accepts(final String pResourceName) {
      return NAME.matcher(pResourceName).matches();
    }

    public void extract(
      final String pResourceName,
      final InputStream pInputStream,
      final BiConsumer<String, String> pDependency
    ) throws IOException {
      new ClassReader(pInputStream).accept(
        new ClassVisitor(Opcodes.ASM9) {
          @Override
          public ModuleVisitor visitModule(
            final String name,
            final int access,
            final String version
          ) {
            return new ModuleVisitor(Opcodes.ASM9) {
              @Override
              public void visitProvide(
                final String service,
                final String... providers
              ) {
                for (String provider : providers) {
                  pDependency.accept(
                    service.replace('/', '.'),
                    provider.replace('/', '.')
                  );
                }
              }
            };
          }
        },
        ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES
      );
    }
  }
}
