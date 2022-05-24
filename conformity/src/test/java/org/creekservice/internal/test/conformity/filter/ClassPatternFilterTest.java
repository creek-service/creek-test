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

package org.creekservice.internal.test.conformity.filter;

import static org.creekservice.internal.test.conformity.filter.ClassPatternFilter.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.testing.EqualsTester;
import java.util.Map;
import org.creekservice.api.test.conformity.test.types.bad.ExampleTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassPatternFilterTest {

    private ClassPatternFilter.Builder builder;

    @BeforeEach
    void setUp() {
        builder = builder();
    }

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        builder().addExclude(".*Test").build(false),
                        builder().addExclude(".*Test").build(false))
                .addEqualityGroup(builder().addExclude(".*Diff").build(false))
                .addEqualityGroup(builder().addExclude(".*Test").build(true))
                .testEquals();
    }

    @Test
    void shouldPassAll() {
        // Given:
        final ClassPatternFilter filter = builder.build(false);

        // Then:
        assertThat(filter.isExcluded(String.class), is(false));
        assertThat(filter.notExcluded(String.class), is(true));
        assertThat(filter.notExcluded(ExampleTest.NestedType.class), is(true));
    }

    @Test
    void shouldExcludeExact() {
        // Given:
        final ClassPatternFilter filter = builder.addExclude(Number.class.getName()).build(false);

        // Then:
        assertThat(filter.isExcluded(Number.class), is(true));
        assertThat(filter.isExcluded(Double.class), is(false));
    }

    @Test
    void shouldExcludePattern() {
        // Given:
        final ClassPatternFilter filter = builder.addExclude("java\\.lang\\..*").build(false);

        // Then:
        assertThat(filter.isExcluded(Number.class), is(true));
        assertThat(filter.isExcluded(Double.class), is(true));
        assertThat(filter.isExcluded(Map.class), is(false));
    }

    @Test
    void shouldCombinePatternsCorrectly() {
        // Given:
        final ClassPatternFilter filter =
                builder.addExclude("java\\.lang\\..*").addExclude("java\\.util\\..*").build(false);

        // Then:
        assertThat(filter.isExcluded(Number.class), is(true));
        assertThat(filter.isExcluded(Map.class), is(true));
    }

    @Test
    void shouldExcludeNestedTypes() {
        // Given:
        final ClassPatternFilter filter = builder.build(true);

        // Then:
        assertThat(filter.isExcluded(ExampleTest.class), is(true));
        assertThat(filter.isExcluded(ExampleTest.NestedType.class), is(true));
    }
}
