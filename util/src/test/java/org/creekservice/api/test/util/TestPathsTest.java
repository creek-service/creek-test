/*
 * Copyright 2021-2024 Creek Contributors (https://github.com/creek-service)
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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.creekservice.api.test.hamcrest.PathMatchers.directory;
import static org.creekservice.api.test.hamcrest.PathMatchers.doesNotExist;
import static org.creekservice.api.test.util.TestPaths.copy;
import static org.creekservice.api.test.util.TestPaths.ensureDirectories;
import static org.creekservice.api.test.util.TestPaths.listDirectory;
import static org.creekservice.api.test.util.TestPaths.listDirectoryRecursive;
import static org.creekservice.api.test.util.TestPaths.moduleRoot;
import static org.creekservice.api.test.util.TestPaths.projectRoot;
import static org.creekservice.api.test.util.TestPaths.readBytes;
import static org.creekservice.api.test.util.TestPaths.readString;
import static org.creekservice.api.test.util.TestPaths.write;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TestPathsTest {

    @TempDir private Path tempDir;

    @Test
    void shouldFindProjectRootWithCurrentModuleName() {
        // When:
        final Path root = projectRoot("util");

        // Then:
        assertThat(root.resolve("util"), is(directory()));
        assertThat(root.resolve("hamcrest"), is(directory()));
    }

    @Test
    void shouldFindProjectRootWithOtherModuleName() {
        // When:
        final Path root = projectRoot("hamcrest");

        // Then:
        assertThat(root.resolve("util"), is(directory()));
        assertThat(root.resolve("hamcrest"), is(directory()));
    }

    @Test
    void shouldFindProjectRootIfCurrentDirectoryIsProject() {
        // Given:
        final Path workingDir = Paths.get("").toAbsolutePath().getParent();

        // When:
        final Path root = projectRoot("util", workingDir);

        // Then:
        assertThat(root.resolve("util"), is(directory()));
        assertThat(root.resolve("hamcrest"), is(directory()));
    }

    @Test()
    void shouldThrowOnProjectRootOnBadModuleName() {
        assertThrows(IllegalArgumentException.class, () -> projectRoot("bad"));
    }

    @Test
    void shouldFindModuleRootWithCurrentModuleName() {
        assertThat(moduleRoot("util").getFileName(), is(Paths.get("util")));
    }

    @Test
    void shouldFindModuleRootWithOtherModuleName() {
        assertThat(moduleRoot("hamcrest").getFileName(), is(Paths.get("hamcrest")));
    }

    @Test()
    void shouldThrowOnModuleRootOnBadModuleName() {
        assertThrows(IllegalArgumentException.class, () -> moduleRoot("bad"));
    }

    @Test
    void shouldListDirectory() {
        // Given:
        ensureDirectories(tempDir.resolve("child1/subChild1"));
        write(tempDir.resolve("child2"), "some-content");
        write(tempDir.resolve("child1/subChild2"), "some-content");

        // When:
        final Stream<Path> result = listDirectory(tempDir);

        // Then:
        final List<String> children =
                result.map(Path::getFileName).map(Objects::toString).collect(Collectors.toList());
        assertThat(children, containsInAnyOrder("child1", "child2"));
    }

    @Test
    void shouldThrowOnErrorListingDirectory() {
        assertThrows(AssertionError.class, () -> listDirectory(tempDir.resolve("I do not exist")));
    }

    @Test
    void shouldListDirectoryRecursively() {
        // Given:
        ensureDirectories(tempDir.resolve("child1/subChild1"));
        write(tempDir.resolve("child2"), "some-content");
        write(tempDir.resolve("child1/subChild2"), "some-content");

        // When:
        final Stream<Path> result = listDirectoryRecursive(tempDir);

        // Then:
        final List<String> children =
                result.map(tempDir::relativize).map(Objects::toString).collect(Collectors.toList());
        assertThat(
                children,
                containsInAnyOrder("child1", "child2", "child1/subChild1", "child1/subChild2"));
    }

    @Test
    void shouldThrowOnErrorListingDirectoryRecursively() {
        assertThrows(
                AssertionError.class,
                () -> listDirectoryRecursive(tempDir.resolve("I do not exist")));
    }

    @Test
    void shouldEnsureDirectories() {
        // Given:
        final Path dir = tempDir.resolve("some/long/path");

        // When:
        ensureDirectories(dir);

        // Then:
        assertThat(dir, is(directory()));
    }

    @Test
    void shouldEnsureDirectoriesEvenIfTheyExist() {
        // Given:
        final Path dir = tempDir.resolve("some/long/path");
        ensureDirectories(dir);

        // When:
        ensureDirectories(dir);

        // Then:
        assertThat(dir, is(directory()));
    }

    @Test
    void shouldThrowOnErrorEnsuringDirectories() {
        // Given:
        final Path filePath = tempDir.resolve("file");
        write(filePath, "contents");

        // Then:
        assertThrows(AssertionError.class, () -> ensureDirectories(tempDir.resolve(filePath)));
    }

    @Test
    void shouldDeleteNonExistent() {
        // Given:
        final Path path = tempDir.resolve("missing");

        // When:
        TestPaths.delete(path);

        // Then: did not throw.
    }

    @Test
    void shouldDeleteFile() {
        // Given:
        final Path path = tempDir.resolve("file");
        write(path, "text");

        // When:
        TestPaths.delete(path);

        // Then:
        assertThat(path, is(doesNotExist()));
    }

    @Test
    void shouldDeleteDirectoryRecursive() {
        // Given:
        final Path dir = tempDir.resolve("dir");
        final Path sub = dir.resolve("sub");
        ensureDirectories(sub);
        write(dir.resolve("file1"), "text");
        write(sub.resolve("file2"), "text");
        write(sub.resolve("file3"), "text");

        // When:
        TestPaths.delete(dir);

        // Then:
        assertThat(dir, is(doesNotExist()));
    }

    @Test
    void shouldReadWriteBytes() {
        // Given:
        final Path path = tempDir.resolve("some/test/path");
        final byte[] expected = "Bob".getBytes(UTF_8);

        // When:
        write(path, expected);
        final byte[] actual = readBytes(path);

        // Then:
        assertThat(actual, is(expected));
    }

    @Test
    void shouldThrowOnErrorReadingBytes() {
        assertThrows(AssertionError.class, () -> readBytes(tempDir.resolve("I do not exist")));
    }

    @Test
    void shouldThrowOnErrorWritingBytes() {
        assertThrows(AssertionError.class, () -> write(tempDir, "contents".getBytes(UTF_8)));
    }

    @Test
    void shouldReadWriteText() {
        // Given:
        final Path path = tempDir.resolve("some/test/path");
        final String expected = "Bob";

        // When:
        write(path, expected);
        final String actual = readString(path);

        // Then:
        assertThat(actual, is(actual));
    }

    @Test
    void shouldThrowOnErrorReadingString() {
        assertThrows(AssertionError.class, () -> readString(tempDir.resolve("I do not exist")));
    }

    @Test
    void shouldThrowOnErrorWritingString() {
        assertThrows(AssertionError.class, () -> write(tempDir, "contents"));
    }

    @Test
    void shouldCopyFile() {
        // Given:
        final Path src = tempDir.resolve("src");
        final Path destination = tempDir.resolve("dest");
        write(src, "text");

        // When:
        copy(src, destination);

        // Then:
        assertThat(readString(destination), is("text"));
    }

    @Test
    void shouldCopyDirectory() {
        // Given:
        final Path src = tempDir.resolve("src");
        final Path destination = tempDir.resolve("dest");
        write(src.resolve("file0"), "text-file0");
        write(src.resolve("dir/file1"), "text-file1");
        TestPaths.ensureDirectories(src.resolve("dir/empty"));

        // When:
        copy(src, destination);

        // Then:
        assertThat(readString(destination.resolve("file0")), is("text-file0"));
        assertThat(readString(destination.resolve("dir/file1")), is("text-file1"));
        assertThat(Files.isDirectory(destination.resolve("dir/empty")), is(true));
    }

    @Test
    void shouldCopyDirectoryIfTargetExists() {
        // Given:
        final Path src = tempDir.resolve("src");
        final Path destination = tempDir.resolve("dest");
        TestPaths.ensureDirectories(destination);
        write(src.resolve("file"), "text");

        // When:
        copy(src, destination);

        // Then:
        assertThat(readString(destination.resolve("file")), is("text"));
    }

    @Test
    void shouldThrowOnCopyIfSourceDoesNotExist() {
        // Given:
        final Path src = tempDir.resolve("src");
        final Path destination = tempDir.resolve("dest");

        // When:
        final Error e = assertThrows(AssertionError.class, () -> copy(src, destination));

        // Then:
        assertThat(e.getMessage(), startsWith("Failed to copy "));
    }
}
