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

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import java.util.Arrays;
import org.creekservice.api.test.conformity.check.CheckConstructorsPrivate;
import org.creekservice.internal.test.conformity.CheckTarget;
import org.creekservice.internal.test.conformity.filter.ClassFilter;
import org.creekservice.internal.test.conformity.filter.PackageFilter;

public final class ConstructorsPrivateCheck implements CheckRunner {

    private static final String NL_INDENT = System.lineSeparator() + "\t";

    private final PackageFilter packageFilter;
    private final ClassFilter classFilter;

    public ConstructorsPrivateCheck(final Options options) {
        this.packageFilter = requireNonNull(options, "options").packageFilter.build();
        this.classFilter = options.classFilter.build();
    }

    @Override
    public String name() {
        return CheckConstructorsPrivate.class.getSimpleName();
    }

    @Override
    public void check(final CheckTarget target) {
        final String failingInfo =
                target.types()
                        .apiClasses()
                        .filter(ClassInfo::isPublic)
                        .filter(ci -> packageFilter.notExcluded(ci.getPackageName()))
                        .filter(ci -> classFilter.notExcluded(ci.loadClass()))
                        .map(this::publicConstructors)
                        .filter(info -> !info.isBlank())
                        .collect(joining(NL_INDENT));

        if (!failingInfo.isBlank()) {
            throw new ApiTypesWithPublicConstructorsException(
                    target.moduleUnderTest().getName(), failingInfo);
        }
    }

    private String publicConstructors(final ClassInfo classInfo) {
        final String publicConstructors =
                classInfo.getDeclaredConstructorInfo().stream()
                        .filter(MethodInfo::isPublic)
                        .map(mi -> mi.toStringWithSimpleNames())
                        .collect(joining(", "));

        return publicConstructors.isBlank()
                ? ""
                : classInfo.getName() + " has public constructors: " + publicConstructors;
    }

    public static final class Options implements CheckConstructorsPrivate {

        private final ClassFilter.Builder classFilter = ClassFilter.builder();
        private final PackageFilter.Builder packageFilter = PackageFilter.builder();

        @Override
        public Options withExcludedClasses(final String justification, final Class<?>... classes) {
            return withExcludedClasses(justification, false, classes);
        }

        @Override
        public Options withExcludedClasses(
                final String justification,
                final boolean excludeSubtypes,
                final Class<?>... classes) {
            if (justification.isBlank()) {
                throw new IllegalArgumentException("justification can not be blank.");
            }

            Arrays.stream(classes).forEach(c -> classFilter.addExclude(c, excludeSubtypes));
            return this;
        }

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

    private static final class ApiTypesWithPublicConstructorsException extends RuntimeException {

        ApiTypesWithPublicConstructorsException(
                final String moduleName, final String failingTypes) {
            super(
                    "API types should not have public constructors. Use factory methods instead."
                            + " module: "
                            + moduleName
                            + ", types: "
                            + failingTypes);
        }
    }
}
