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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Filter for excluding classes based on a pattern. */
public final class ClassPatternFilter {

    private final Pattern excluded;

    public static Builder builder() {
        return new Builder();
    }

    private ClassPatternFilter(final Pattern excluded) {
        this.excluded = requireNonNull(excluded, "excluded");
    }

    /**
     * Test if a type <i>is</i> excluded.
     *
     * @param type the type.
     * @return {@code true} if excluded.
     */
    public boolean isExcluded(final Class<?> type) {
        return excluded.matcher(type.getName()).matches();
    }

    /**
     * Test if a type is <i>not</i> excluded.
     *
     * @param type the type.
     * @return {@code false} if excluded.
     */
    public boolean notExcluded(final Class<?> type) {
        return !isExcluded(type);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ClassPatternFilter that = (ClassPatternFilter) o;
        return Objects.equals(excluded.pattern(), that.excluded.pattern());
    }

    @Override
    public int hashCode() {
        return Objects.hash(excluded.pattern());
    }

    @Override
    public String toString() {
        return "ClassPatternFilter{" + "excluded=" + excluded + '}';
    }

    public static final class Builder {

        private final List<Pattern> excluded = new ArrayList<>();

        private Builder() {}

        /**
         * @param pattern the pattern to exclude. Matched against the fully qualified class name.
         * @return self.
         */
        public Builder addExclude(final String pattern) {
            return addExclude(Pattern.compile(pattern));
        }

        /**
         * @param pattern the pattern to exclude. Matched against the fully qualified class name.
         * @return self.
         */
        public Builder addExclude(final Pattern pattern) {
            excluded.add(pattern);
            return this;
        }

        /** @return the built filter.
         * @param excludeTestClasses if {@code true}, the filter will exclude test classes and any types nested within
         *                           using the pattern {@code .*Test(\$.*)?}.
         */
        public ClassPatternFilter build(final boolean excludeTestClasses) {
            final Stream<String> patterns = excluded.stream()
                    .map(Pattern::pattern);

            final Stream<String> all = excludeTestClasses
                    ? Stream.concat(patterns, Stream.of(".*Test(\\$.*)?"))
                    : patterns;

            final String combined =
                    all
                            .reduce((l, r) -> l + "|" + r)
                            .orElse("");

            return new ClassPatternFilter(Pattern.compile(combined));
        }
    }
}
