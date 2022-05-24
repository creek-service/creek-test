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
import java.util.function.Predicate;

/** Filter for excluding classes. */
public final class ClassFilter {

    private final List<Predicate<Class<?>>> excluded;

    public static Builder builder() {
        return new Builder();
    }

    private ClassFilter(final List<Predicate<Class<?>>> excluded) {
        this.excluded = List.copyOf(requireNonNull(excluded, "excluded"));
    }

    /**
     * Test if a type <i>is</i> excluded.
     *
     * @param type the type.
     * @return {@code true} if excluded.
     */
    public boolean isExcluded(final Class<?> type) {
        return excluded.stream().anyMatch(predicate -> predicate.test(type));
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
        final ClassFilter that = (ClassFilter) o;
        return Objects.equals(excluded, that.excluded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(excluded);
    }

    @Override
    public String toString() {
        return "ClassFilter{" + "excluded=" + excluded + '}';
    }

    public static final class Builder {

        private final List<Predicate<Class<?>>> excluded = new ArrayList<>();

        private Builder() {}

        /**
         * @param type the type to exclude.
         * @return self.
         */
        public Builder addExclude(final Class<?> type, final boolean excludeSubtypes) {
            excluded.add(excludeSubtypes ? new ExcludedHierarchy(type) : new ExcludedType(type));
            return this;
        }

        /** @return the built filter. */
        public ClassFilter build() {
            return new ClassFilter(excluded);
        }
    }

    private static final class ExcludedType implements Predicate<Class<?>> {

        private final Class<?> excludedType;

        ExcludedType(final Class<?> excludedType) {
            this.excludedType = requireNonNull(excludedType, "excludedType");
        }

        @Override
        public boolean test(final Class<?> testType) {
            return Objects.equals(excludedType, testType);
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof ExcludedType)) {
                return false;
            }
            final ExcludedType that = (ExcludedType) o;
            return Objects.equals(excludedType, that.excludedType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(excludedType);
        }

        @Override
        public String toString() {
            return excludedType.getName();
        }
    }

    private static final class ExcludedHierarchy implements Predicate<Class<?>> {

        private final Class<?> excludedType;

        ExcludedHierarchy(final Class<?> excludedType) {
            this.excludedType = requireNonNull(excludedType, "excludedType");
        }

        @Override
        public boolean test(final Class<?> testType) {
            return excludedType.isAssignableFrom(testType);
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof ExcludedHierarchy)) {
                return false;
            }
            final ExcludedHierarchy that = (ExcludedHierarchy) o;
            return Objects.equals(excludedType, that.excludedType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(excludedType);
        }

        @Override
        public String toString() {
            return excludedType.getName() + "*";
        }
    }
}
