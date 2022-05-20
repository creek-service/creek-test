/*
 * Copyright 2021-2022 Creek Contributors (https://github.com/creek-service)
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

import static java.nio.file.Files.isDirectory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/** Utility methods for working with paths, directories and files in tests. */
public final class TestPaths {

    private TestPaths() {}

    /**
     * Find the root project path of a multi-module project.
     *
     * <p>Depending on how tests are executed the current directory may be the project root or a
     * module root. This method finds the project root, given any known module name.
     *
     * @param knownModuleName any known module in the project
     * @return the path to the root of the project.
     */
    public static Path projectRoot(final String knownModuleName) {
        return projectRoot(knownModuleName, Paths.get("").toAbsolutePath());
    }

    /**
     * Find the root project path of a multi-module project, given the current working directory.
     *
     * <p>Depending on how tests are executed the current directory may be the project root or a
     * module root. This method finds the project root, given any known module name.
     *
     * @param knownModuleName any known module in the project
     * @param currentDir the current working directory.
     * @return the path to the root of the project.
     */
    public static Path projectRoot(final String knownModuleName, final Path currentDir) {
        if (isDirectory(currentDir.resolve(knownModuleName))) {
            return currentDir;
        }

        final Path parent = currentDir.getParent();
        if (parent != null && isDirectory(parent.resolve(knownModuleName))) {
            return parent;
        }

        throw new IllegalArgumentException("Invalid known module name: " + knownModuleName);
    }

    /**
     * Find the path to a module within a multi-module project.
     *
     * @param moduleName any known module in the project
     * @return the root of the module within the repo/project.
     */
    public static Path moduleRoot(final String moduleName) {
        return projectRoot(moduleName).resolve(moduleName);
    }

    /**
     * List directory content (non-recursive).
     *
     * @param path the directory to list
     * @return stream of entries in the directory
     */
    public static Stream<Path> listDirectory(final Path path) {
        try {
            return Files.list(path);
        } catch (final IOException e) {
            throw new AssertionError("Failed to list directory: " + path, e);
        }
    }

    /**
     * Create a directory and all parent directories if they don't exist.
     *
     * @param path the path to create
     */
    public static void ensureDirectories(final Path path) {
        try {
            Files.createDirectories(path);
        } catch (final IOException e) {
            throw new AssertionError("Failed to create directory: " + path, e);
        }
    }

    /**
     * Create the parent directories of the supplied {@code path}, if they don't exist.
     *
     * <p>Given a {@code path} or {@code a/b/c}, this method will ensure {@code a/b} exists as
     * directories.
     *
     * @param path the path to create the parent of
     */
    public static void ensureParent(final Path path) {
        final Path parent = path.getParent();
        if (parent != null) {
            ensureDirectories(parent);
        }
    }

    /**
     * Read the contents of a file as a bytes.
     *
     * @param path path to read
     */
    public static byte[] readBytes(final Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (final IOException e) {
            throw new AssertionError("Failed to read file: " + path, e);
        }
    }

    /**
     * Read the contents of a file as a String.
     *
     * @param path path to read
     */
    public static String readString(final Path path) {
        try {
            return Files.readString(path);
        } catch (final IOException e) {
            throw new AssertionError("Failed to read file: " + path, e);
        }
    }

    /**
     * Write the supplied {@code bytes} to a file at the supplied {@code path}.
     *
     * <p>The file and parent directories are created if they do not already exist.
     *
     * @param path the path to the file
     * @param bytes the binary data to write
     */
    public static void write(final Path path, final byte[] bytes) {
        try {
            ensureParent(path);
            Files.write(path, bytes);
        } catch (final IOException e) {
            throw new AssertionError("Failed to write file: " + path, e);
        }
    }

    /**
     * Write the supplied {@code text} to a file at the supplied {@code path}.
     *
     * <p>The file and parent directories are created if they do not already exist.
     *
     * <p>The supplied {@code text} is written as {@code UTF-8}.
     *
     * @param path the path to the file
     * @param text the text to write
     */
    public static void write(final Path path, final String text) {
        write(path, text.getBytes(StandardCharsets.UTF_8));
    }
}
