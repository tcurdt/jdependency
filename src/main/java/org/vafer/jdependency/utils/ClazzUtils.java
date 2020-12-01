/*
 * Copyright 2010-2020 The jdependency developers.
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
package org.vafer.jdependency.utils;

import java.io.IOException;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.security.MessageDigest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.MessageDigestCalculatingInputStream;

import org.vafer.jdependency.Clazz;
import org.vafer.jdependency.ClazzpathUnit;

public final class ClazzUtils {

    private static MessageDigest digestForPath(String absolutePath) throws IOException {
        try {
            final MessageDigestCalculatingInputStream input =
                new MessageDigestCalculatingInputStream(
                    new FileInputStream(absolutePath), "SHA-256");

            org.apache.commons.io.IOUtils.consume(input);

            input.close();

            return input.getMessageDigest();

        } catch(java.security.NoSuchAlgorithmException e) {
          throw new IOException("hell just froze over", e);
        }
    }

    public static int versions(final Clazz clazz) throws IOException {
        final Set<MessageDigest> digests = new HashSet<>();
        for (ClazzpathUnit u : clazz.getClazzpathUnits()) {
            digests.add(digestForPath(u.toString()));
        }
        return digests.size();
    }

    public static Set<Clazz> uniq(Set<Clazz> clazzes) throws IOException {
        final Set<Clazz> result = new HashSet<>();
        for (Clazz clazz : clazzes) {
            if (versions(clazz) == 1) {
                result.add(clazz);
            }
        }
        return result;
    }

}
