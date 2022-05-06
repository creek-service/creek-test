/*
 * Copyright 2022 Creek Contributors (https://github.com/creek-service)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.creekservice.api.test.util;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Helper to get a temporary directory that is cleaned down when the test exists.
 *
 * <p>Works around the issue that directories returned by {@link Files#createTempDirectory} will not
 * be deleted on exit if they are not empty.
 */
public final class Temp {

    private Temp() {}

    public static Path tempDir(final String prefix) {
        try {
            final Path path = Files.createTempDirectory(prefix).toAbsolutePath();
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> recursiveDelete(path, Files::delete)));
            return path;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to create temporary directory", e);
        }
    }

    // @VisibleForTesting
    static void recursiveDelete(final Path path, final FileDeleteMethod delete) {
        try {
            Files.walkFileTree(
                    path,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult visitFile(
                                final Path file, final BasicFileAttributes attrs)
                                throws IOException {
                            delete.delete(file);
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(
                                final Path dir, final IOException e) throws IOException {
                            if (e != null) {
                                throw e;
                            }
                            delete.delete(dir);
                            return FileVisitResult.CONTINUE;
                        }
                    });
        } catch (IOException e) {
            System.err.println("Failed to delete state directory: " + path);
            e.printStackTrace(new PrintWriter(System.err, true, StandardCharsets.UTF_8));
        }
    }

    // @VisibleForTesting
    interface FileDeleteMethod {
        void delete(Path path) throws IOException;
    }
}
