/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

import static org.creekservice.internal.test.conformity.filter.PackageFilter.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PackageFilterTest {

    private PackageFilter.Builder builder;

    @BeforeEach
    void setUp() {
        builder = builder();
    }

    @Test
    void shouldImplementHashCodeAndEquals() {
        new EqualsTester()
                .addEqualityGroup(
                        builder().addExclude("some.package").build(),
                        builder().addExclude("some.package").build())
                .addEqualityGroup(builder().build())
                .addEqualityGroup(builder().addExclude("different.package").build())
                .addEqualityGroup(builder().addExclude("some.package.*").build())
                .addEqualityGroup(builder().addExclude("some.package.sub").build())
                .testEquals();
    }

    @Test
    void shouldPassAll() {
        // Given:
        final PackageFilter filter = builder.build();

        // Then:
        assertThat(filter.isExcluded("any.old.package"), is(false));
        assertThat(filter.notExcluded("any.old.package"), is(true));
    }

    @Test
    void shouldExcludeExact() {
        // Given:
        final PackageFilter filter = builder.addExclude("some.exact.package").build();

        // Then:
        assertThat(filter.isExcluded("some.exact.package"), is(true));
        assertThat(filter.isExcluded("some.exact.package.sub"), is(false));
        assertThat(filter.isExcluded("any.old.package"), is(false));
    }

    @Test
    void shouldExcludeWildcard() {
        // Given:
        final PackageFilter filter = builder.addExclude("some.package.*").build();

        // Then:
        assertThat(filter.isExcluded("some"), is(false));
        assertThat(filter.isExcluded("some.package"), is(true));
        assertThat(filter.isExcluded("some.package.sub"), is(true));
        assertThat(filter.isExcluded("some.package.deep.sub"), is(true));
        assertThat(filter.isExcluded("any.old.package"), is(false));
    }
}
