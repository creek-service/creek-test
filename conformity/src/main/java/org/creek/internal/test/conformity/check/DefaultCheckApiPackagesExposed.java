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

package org.creek.internal.test.conformity.check;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.creek.internal.test.conformity.Constants.API_PACKAGE;

import java.util.Arrays;
import java.util.function.Predicate;
import org.creek.api.test.conformity.CheckTarget;
import org.creek.api.test.conformity.ConformityCheck;
import org.creek.api.test.conformity.check.CheckApiPackagesExposed;
import org.creek.internal.test.conformity.PackageFilter;

public final class DefaultCheckApiPackagesExposed implements ConformityCheck {

    private static final String NL_INDENT = System.lineSeparator() + "\t";

    private final Predicate<String> packageFilter;

    private DefaultCheckApiPackagesExposed(final Predicate<String> packageFilter) {
        this.packageFilter = requireNonNull(packageFilter, "packageFilter");
    }

    @Override
    public String name() {
        return CheckApiPackagesExposed.class.getSimpleName();
    }

    @Override
    public void check(final CheckTarget target) {
        final Module moduleUnderTest = target.moduleUnderTest();

        final String notExposed =
                moduleUnderTest.getPackages().stream()
                        .filter(pkg -> pkg.startsWith(API_PACKAGE))
                        .filter(packageFilter)
                        .filter(pkg -> !moduleUnderTest.isExported(pkg))
                        .sorted()
                        .collect(joining(NL_INDENT));

        if (!notExposed.isEmpty()) {
            throw new ApiPackageNotExposedException(moduleUnderTest.getName(), notExposed);
        }
    }

    public static final class Builder implements CheckApiPackagesExposed {

        private final PackageFilter.Builder packageFilter = PackageFilter.builder();

        /**
         * Exclude one or more packages from the test.
         *
         * @param packageNames packages to ignore. Any name ending in `.*` will ignore all
         *     sub-packages too.
         * @return self.
         */
        @Override
        public Builder excludedPackages(final String... packageNames) {
            Arrays.stream(packageNames).forEach(packageFilter::addExclude);
            return this;
        }

        @Override
        public DefaultCheckApiPackagesExposed build() {
            return new DefaultCheckApiPackagesExposed(packageFilter.build()::notExcluded);
        }
    }

    private static final class ApiPackageNotExposedException extends RuntimeException {

        ApiPackageNotExposedException(final String moduleName, final String notExposed) {
            super(
                    "Some API packages are not exposed in the module's module-info.java file. module="
                            + moduleName
                            + ", unexposed_packages=["
                            + NL_INDENT
                            + notExposed
                            + System.lineSeparator()
                            + "]");
        }
    }
}
