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

package org.creekservice.internal.test.conformity.check;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.creekservice.internal.test.conformity.Constants.API_PACKAGE;

import java.util.Arrays;
import java.util.function.Predicate;
import org.creekservice.api.test.conformity.check.CheckExportedPackages;
import org.creekservice.internal.test.conformity.CheckTarget;
import org.creekservice.internal.test.conformity.filter.PackageFilter;

/** Checks only API packages are exported to all. */
public final class ExportedPackagesCheck implements CheckRunner {

    private static final String NL_INDENT = System.lineSeparator() + "\t";

    private final Predicate<String> packageFilter;

    /**
     * @param options options to control behaviour
     */
    public ExportedPackagesCheck(final Options options) {
        this.packageFilter = requireNonNull(options, "options").packageFilter.build()::notExcluded;
    }

    @Override
    public String name() {
        return CheckExportedPackages.class.getSimpleName();
    }

    @Override
    public void check(final CheckTarget target) {
        final Module moduleUnderTest = target.moduleUnderTest();
        if (!moduleUnderTest.isNamed() || moduleUnderTest.getDescriptor().isAutomatic()) {
            // Do not test unnamed/automatic modules, as everything is exposed.
            // The fact a module is unnamed/automatic will be picked up by CheckModule
            return;
        }

        checkApiPackagesExported(moduleUnderTest);
        checkNonApiPackagesNotExported(moduleUnderTest);
    }

    private void checkApiPackagesExported(final Module moduleUnderTest) {
        final String notExported =
                moduleUnderTest.getPackages().stream()
                        .filter(packageFilter)
                        .filter(pkg -> pkg.startsWith(API_PACKAGE))
                        .filter(pkg -> !moduleUnderTest.isExported(pkg))
                        .sorted()
                        .collect(joining(NL_INDENT));

        if (!notExported.isEmpty()) {
            throw new ApiPackageNotExposedException(moduleUnderTest.getName(), notExported);
        }
    }

    private void checkNonApiPackagesNotExported(final Module moduleUnderTest) {
        final String exported =
                moduleUnderTest.getPackages().stream()
                        .filter(packageFilter)
                        .filter(pkg -> !pkg.startsWith(API_PACKAGE))
                        .filter(moduleUnderTest::isExported)
                        .sorted()
                        .collect(joining(NL_INDENT));

        if (!exported.isEmpty()) {
            throw new NonApiPackageExposedException(moduleUnderTest.getName(), exported);
        }
    }

    /** Options to configure this check */
    public static final class Options implements CheckExportedPackages {

        private final PackageFilter.Builder packageFilter = PackageFilter.builder();

        @Override
        public Options withExcludedPackages(
                final String justification, final String... packageNames) {
            if (justification.isBlank()) {
                throw new IllegalArgumentException("justification can not be blank.");
            }

            Arrays.stream(packageNames).forEach(packageFilter::addExclude);
            return this;
        }
    }

    private static final class ApiPackageNotExposedException extends RuntimeException {

        ApiPackageNotExposedException(final String moduleName, final String notExposed) {
            super(
                    "API packages are not exposed in the module's module-info.java file. module="
                            + moduleName
                            + ", unexposed_packages=["
                            + NL_INDENT
                            + notExposed
                            + System.lineSeparator()
                            + "]");
        }
    }

    private static final class NonApiPackageExposedException extends RuntimeException {

        NonApiPackageExposedException(final String moduleName, final String exposed) {
            super(
                    "Non-API packages are exposed (without a 'to' clause) in the module's module-info.java file."
                            + " module="
                            + moduleName
                            + ", exposed_packages=["
                            + NL_INDENT
                            + exposed
                            + System.lineSeparator()
                            + "]");
        }
    }
}
