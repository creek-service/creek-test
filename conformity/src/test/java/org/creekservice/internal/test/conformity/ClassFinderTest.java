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

package org.creekservice.internal.test.conformity;

import static org.hamcrest.MatcherAssert.assertThat;

import io.github.classgraph.ClassInfo;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.test.conformity.ConformityTester;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClassFinderTest {

    private static ClassFinder finder;

    @BeforeAll
    static void setUp() {
        finder = new ClassFinder(ClassFinderTest.class);
    }

    @AfterAll
    static void afterAll() {
        finder.close();
    }

    @Test
    void shouldReturnCreekClasses() {
        assertThat(
                "should find api class",
                finder.classes()
                        .map(ClassInfo::getSimpleName)
                        .anyMatch(
                                className -> className.equals(CheckTarget.class.getSimpleName())));

        assertThat(
                "should find internal class",
                finder.classes()
                        .map(ClassInfo::getSimpleName)
                        .anyMatch(
                                className ->
                                        className.equals(ClassFinderTest.class.getSimpleName())));
    }

    @Test
    void shouldReturnApiClasses() {
        assertThat(
                "should find api class",
                finder.apiClasses()
                        .map(ClassInfo::getSimpleName)
                        .anyMatch(
                                className ->
                                        className.equals(ConformityTester.class.getSimpleName())));

        assertThat(
                "should not find internal class",
                finder.apiClasses()
                        .map(ClassInfo::getSimpleName)
                        .noneMatch(
                                className ->
                                        className.equals(ClassFinderTest.class.getSimpleName())));
    }

    @Test
    void shouldNotReturnCreekClassesFromOtherModules() {
        assertThat(
                "from classes",
                finder.classes()
                        .map(ClassInfo::getSimpleName)
                        .noneMatch(
                                className ->
                                        className.equals(VisibleForTesting.class.getSimpleName())));

        assertThat(
                "from api classes",
                finder.apiClasses()
                        .map(ClassInfo::getSimpleName)
                        .noneMatch(
                                className ->
                                        className.equals(VisibleForTesting.class.getSimpleName())));
    }

    @Test
    void shouldNotReturnNonCreekClasses() {
        assertThat(
                "from classes",
                finder.classes()
                        .map(ClassInfo::getSimpleName)
                        .noneMatch(className -> className.equals(ClassInfo.class.getSimpleName())));

        assertThat(
                "from api classes",
                finder.apiClasses()
                        .map(ClassInfo::getSimpleName)
                        .noneMatch(className -> className.equals(ClassInfo.class.getSimpleName())));
    }

    @Test
    void shouldReturnNestedClasses() {
        assertThat(
                "inner",
                finder.classes()
                        .map(ClassInfo::getSimpleName)
                        .anyMatch(
                                className -> className.equals(NestedClass.class.getSimpleName())));

        assertThat(
                "inner",
                finder.classes()
                        .map(ClassInfo::getSimpleName)
                        .anyMatch(
                                className ->
                                        className.equals(StaticNestedClass.class.getSimpleName())));
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public final class NestedClass {}

    public static final class StaticNestedClass {}
}
