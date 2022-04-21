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

package org.creek.internal.test.conformity;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Filter for excluding packages. */
public final class PackageFilter {

    private static final String PKG_WILDCARD = ".*";

    private final Map<String, Boolean> excluded;

    public static Builder builder() {
        return new Builder();
    }

    private PackageFilter(final Map<String, Boolean> excluded) {
        this.excluded = Map.copyOf(requireNonNull(excluded, "excluded"));
    }

    /**
     * Test if a package <i>is</i> excluded.
     *
     * @param packageName the package name.
     * @return {@code true} if excluded.
     */
    public boolean isExcluded(final String packageName) {
        boolean full = true;
        String remaining = packageName;

        while (!remaining.isEmpty()) {
            final Boolean wildcard = excluded.get(remaining);
            if (wildcard != null && (wildcard || full)) {
                return true;
            }

            full = false;
            remaining = removeTailPackage(remaining);
        }

        return false;
    }

    /**
     * Test if a package <i>not</i> is excluded.
     *
     * @param packageName the package name.
     * @return {@code false} if excluded.
     */
    public boolean notExcluded(final String packageName) {
        return !isExcluded(packageName);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final PackageFilter that = (PackageFilter) o;
        return Objects.equals(excluded, that.excluded);
    }

    @Override
    public int hashCode() {
        return Objects.hash(excluded);
    }

    @Override
    public String toString() {
        return "PackageFilter{" + "excluded=" + excluded + '}';
    }

    private static String removeTailPackage(final String pkg) {
        final int i = pkg.lastIndexOf('.');
        return i == -1 ? "" : pkg.substring(0, i);
    }

    private static boolean endsInWildcard(final String pkg) {
        return pkg.endsWith(PKG_WILDCARD);
    }

    private static String trimWildcard(final String pkg) {
        return endsInWildcard(pkg) ? pkg.substring(0, pkg.length() - PKG_WILDCARD.length()) : pkg;
    }

    public static final class Builder {

        private final Map<String, Boolean> excluded = new HashMap<>();

        private Builder() {}

        /**
         * @param packageName the package to exclude. If it ends in {@code .*} sub packages will be
         *     excluded also.
         * @return self.
         */
        public Builder addExclude(final String packageName) {
            excluded.put(trimWildcard(packageName), endsInWildcard(packageName));
            return this;
        }

        /** @return the built filter. */
        public PackageFilter build() {
            return new PackageFilter(excluded);
        }
    }
}
