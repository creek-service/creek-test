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

package org.creek.api.test.hamcrest;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Description;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeDiagnosingMatcher;

/** Hamcrest matchers for working with {@link Path paths}. */
public final class PathMatchers {

    private PathMatchers() {}

    /** Check a path is a regular file. */
    public static Matcher<Path> regularFile() {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(
                    final Path path, final Description mismatchDescription) {
                if (!Files.exists(path)) {
                    mismatchDescription.appendValue(path).appendText(" does not exist");
                    return false;
                }
                if (!Files.isRegularFile(path)) {
                    mismatchDescription.appendValue(path).appendText(" is not a regular file");
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("a regular file");
            }
        };
    }

    /** Check a path is a directory. */
    public static Matcher<Path> directory() {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(
                    final Path path, final Description mismatchDescription) {
                if (!Files.exists(path)) {
                    mismatchDescription.appendValue(path).appendText(" does not exists");
                    return false;
                }
                if (!Files.isDirectory(path)) {
                    mismatchDescription.appendValue(path).appendText(" is not a directory");
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("a directory");
            }
        };
    }

    /** Check a path does not exist as a file or directory. */
    public static Matcher<Path> doesNotExist() {
        return new TypeSafeDiagnosingMatcher<>() {
            @Override
            protected boolean matchesSafely(
                    final Path path, final Description mismatchDescription) {
                if (Files.exists(path)) {
                    mismatchDescription.appendValue(path).appendText(" does exist");
                    return false;
                }
                return true;
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("does not exist");
            }
        };
    }

    /**
     * Check a directory contains files and directories with the supplied {@code names}.
     *
     * @param names the expected complete set of children in the directory.
     */
    public static Matcher<Path> directoryChildren(final String... names) {
        return directoryChildren(containsInAnyOrder(names));
    }

    /**
     * Check the names of the children in a directory match the supplied {@code childrenMatcher}.
     *
     * @param childrenMatcher the matcher invoked with the full set of child names
     */
    public static Matcher<Path> directoryChildren(
            final Matcher<? super Collection<String>> childrenMatcher) {

        final FeatureMatcher<Path, List<String>> contentIsAsExpected =
                new FeatureMatcher<>(childrenMatcher, "contents is", "content") {
                    @Override
                    protected List<String> featureValueOf(final Path path) {
                        try (Stream<Path> files = Files.list(path)) {
                            return files.map(path::relativize)
                                    .map(Path::toString)
                                    .collect(Collectors.toList());
                        } catch (IOException e) {
                            throw new AssertionError("Failed to list directory: " + path, e);
                        }
                    }
                };

        return Matchers.both(is(directory())).and(contentIsAsExpected);
    }

    /**
     * Check the content of a file contains the supplied {@code text}.
     *
     * @param text the text the file should contain.
     */
    public static Matcher<Path> fileContains(final String text) {
        return fileContains(containsString(text));
    }

    /**
     * Check the content of a file matches the supplied {@code contentMatcher}.
     *
     * @param contentMatcher the matcher that is invoked with the file contents.
     */
    public static Matcher<Path> fileContains(final Matcher<String> contentMatcher) {
        final FeatureMatcher<Path, String> contentIsAsExpected =
                new FeatureMatcher<>(contentMatcher, "contents is", "content") {
                    @Override
                    protected String featureValueOf(final Path path) {
                        try {
                            return Files.readString(path);
                        } catch (IOException e) {
                            throw new AssertionError("Failed to load content from: " + path, e);
                        }
                    }
                };

        return Matchers.both(is(regularFile())).and(contentIsAsExpected);
    }
}
