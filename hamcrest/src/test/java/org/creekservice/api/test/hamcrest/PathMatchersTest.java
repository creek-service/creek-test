/*
 * Copyright 2021-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.test.hamcrest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.creekservice.api.test.hamcrest.PathMatchers.directory;
import static org.creekservice.api.test.hamcrest.PathMatchers.directoryChildren;
import static org.creekservice.api.test.hamcrest.PathMatchers.doesNotExist;
import static org.creekservice.api.test.hamcrest.PathMatchers.fileContains;
import static org.creekservice.api.test.hamcrest.PathMatchers.regularFile;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

import java.nio.file.Files;
import java.nio.file.Path;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PathMatchersTest {

    @TempDir private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        Files.createFile(tempDir.resolve("file"));
        Files.createDirectory(tempDir.resolve("dir"));
    }

    @Nested
    class RegularFileTest {
        @Test
        void shouldPassIfFile() {
            assertThat(tempDir.resolve("file"), is(regularFile()));
        }

        @Test
        void shouldFailIfDoesNotExist() {
            assertThat(tempDir.resolve("does not exist"), is(not(regularFile())));
        }

        @Test
        void shouldFailIfNotRegularFile() {
            assertThat(tempDir.resolve("dir"), is(not(regularFile())));
        }
    }

    @Nested
    class DirectoryTest {
        @Test
        void shouldPassIfDirectory() {
            assertThat(tempDir.resolve("dir"), is(directory()));
        }

        @Test
        void shouldFailIfDoesNotExist() {
            assertThat(tempDir.resolve("does not exist"), is(not(directory())));
        }

        @Test
        void shouldPassNegativeIfNotDirectory() {
            assertThat(tempDir.resolve("file"), is(not(directory())));
        }
    }

    @Nested
    class DoesNotExistTest {
        @Test
        void shouldPassIfDoesNotExist() {
            assertThat(tempDir.resolve("does.not.exist"), doesNotExist());
        }

        @Test
        void shouldFailIfExists() {
            assertThat(tempDir.resolve("file"), not(doesNotExist()));
            assertThat(tempDir.resolve("dir"), not(doesNotExist()));
        }
    }

    @Nested
    class DirectoryChildrenTest {

        @Test
        void shouldPassIfChildNamesMatch() {
            assertThat(tempDir, directoryChildren("file", "dir"));
        }

        @Test
        void shouldPassRegardlessOfOrder() {
            assertThat(tempDir, directoryChildren("dir", "file"));
        }

        @Test
        void shouldPassIfMatcherMatches() {
            assertThat(tempDir, directoryChildren(hasSize(2)));
        }

        @Test
        void shouldFailIfNotDirectory() {
            assertThat(tempDir.resolve("file"), not(directoryChildren("file", "not found")));
        }

        @Test
        void shouldFailIfNamesMismatch() {
            assertThat(tempDir, not(directoryChildren("file", "not found")));
        }

        @Test
        void shouldFailIfMatcherMismatch() {
            assertThat(tempDir, not(directoryChildren(hasItem("not found"))));
        }
    }

    @Nested
    class FileContainsTest {

        private Path file;

        @BeforeEach
        public void setUp() throws Exception {
            file = tempDir.resolve("file");
            Files.write(file, "Why am I Mr. Pink?".getBytes(UTF_8));
        }

        @Test
        void shouldPassIfTextMatches() {
            assertThat(file, fileContains("Mr. Pink"));
        }

        @Test
        void shouldPassIfMatcherMatches() {
            assertThat(file, fileContains(both(startsWith("Why")).and(endsWith("?"))));
        }

        @Test
        void shouldFailIfContentDoesNotContainText() {
            assertThat(file, not(fileContains("you won't find me, right")));
        }

        @Test
        void shouldFailIfMatcherDoesNotMatch() {
            assertThat(file, not(fileContains(containsString("you won't find me, right"))));
        }
    }

    @Nested
    class DescriptionTest {
        private final org.hamcrest.Description description = new StringDescription();

        @Test
        void shouldDescribeRegularFile() {
            assertThat(description(regularFile()), is("a regular file"));
        }

        @Test
        void shouldDescribeDirectory() {
            assertThat(description(directory()), is("a directory"));
        }

        @Test
        void shouldDescribeDoesNotExist() {
            assertThat(description(doesNotExist()), is("does not exist"));
        }

        @Test
        void shouldDescribeDirectoryChildren() {
            assertThat(
                    description(directoryChildren("a", "b")),
                    is(
                            "(is a directory and contents is iterable with items [\"a\", \"b\"] in"
                                    + " any order)"));
        }

        @Test
        void shouldDescribeFileContents() {
            assertThat(
                    description(fileContains("text")),
                    is("(is a regular file and contents is a string containing \"text\")"));
        }

        private String description(final Matcher<?> matcher) {
            matcher.describeTo(description);
            return description.toString();
        }
    }
}
