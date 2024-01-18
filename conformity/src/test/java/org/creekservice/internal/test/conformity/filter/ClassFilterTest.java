/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

import static org.creekservice.internal.test.conformity.filter.ClassFilter.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClassFilterTest {

    private ClassFilter.Builder builder;

    @BeforeEach
    void setUp() {
        builder = builder();
    }

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        builder().addExclude(String.class, false).build(),
                        builder().addExclude(String.class, false).build())
                .addEqualityGroup(builder().build())
                .addEqualityGroup(builder().addExclude(Integer.class, false).build())
                .addEqualityGroup(builder().addExclude(String.class, true).build())
                .addEqualityGroup(builder().addExclude(Integer.class, true).build())
                .testEquals();
    }

    @Test
    void shouldPassAll() {
        // Given:
        final ClassFilter filter = builder.build();

        // Then:
        assertThat(filter.isExcluded(String.class), is(false));
        assertThat(filter.notExcluded(String.class), is(true));
    }

    @Test
    void shouldExcludeExact() {
        // Given:
        final ClassFilter filter = builder.addExclude(Number.class, false).build();

        // Then:
        assertThat(filter.isExcluded(Number.class), is(true));
        assertThat(filter.isExcluded(Double.class), is(false));
    }

    @Test
    void shouldExcludeSubtypes() {
        // Given:
        final ClassFilter filter = builder.addExclude(Number.class, true).build();

        // Then:
        assertThat(filter.isExcluded(Number.class), is(true));
        assertThat(filter.isExcluded(Double.class), is(true));
    }
}
