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

package org.creekservice.internal.test.conformity;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.ExcludesClasses;
import org.creekservice.api.test.conformity.ExcludesPackages;
import org.creekservice.api.test.conformity.check.CheckConstructorsPrivate;
import org.creekservice.api.test.conformity.check.CheckExportedPackages;
import org.creekservice.api.test.conformity.check.CheckModule;
import org.creekservice.api.test.conformity.check.ConformityCheck;
import org.creekservice.internal.test.conformity.check.CheckRunner;
import org.creekservice.internal.test.conformity.check.ConstructorsPrivateCheck;
import org.creekservice.internal.test.conformity.check.ExportedPackagesCheck;
import org.creekservice.internal.test.conformity.check.ModuleCheck;

/** Default implementation of {@link ConformityTester} */
public final class DefaultConformityTester implements ConformityTester {

    private static final List<Supplier<ConformityCheck>> DEFAULT_OPTIONS =
            List.of(
                    CheckModule::builder,
                    CheckExportedPackages::builder,
                    CheckConstructorsPrivate::builder);

    private static final Map<Class<? extends ConformityCheck>, CheckRunnerFactory<?>> RUNNERS =
            Map.of(
                    ModuleCheck.Options.class,
                    options -> new ModuleCheck((ModuleCheck.Options) options),
                    ExportedPackagesCheck.Options.class,
                    options -> new ExportedPackagesCheck((ExportedPackagesCheck.Options) options),
                    ConstructorsPrivateCheck.Options.class,
                    options ->
                            new ConstructorsPrivateCheck(
                                    (ConstructorsPrivateCheck.Options) options));

    private final Class<?> typeFromModuleToTest;
    private final Map<Class<? extends ConformityCheck>, ConformityCheck> options = new HashMap<>();

    /**
     * Create instance.
     *
     * @param typeFromModuleToTest any type from the module under test.
     */
    public DefaultConformityTester(final Class<?> typeFromModuleToTest) {
        this.typeFromModuleToTest = requireNonNull(typeFromModuleToTest, "typeFromModuleToTest");

        DEFAULT_OPTIONS.forEach(
                supplier -> {
                    final ConformityCheck check = supplier.get();
                    options.put(check.getClass(), check);
                });
    }

    @Override
    public DefaultConformityTester withCustom(final ConformityCheck check) {
        runnerFactory(check);
        this.options.put(check.getClass(), check);
        return this;
    }

    @Override
    public DefaultConformityTester withDisabled(
            final String justification, final ConformityCheck check) {
        if (justification.isBlank()) {
            throw new IllegalArgumentException("justification can not be blank.");
        }

        options.remove(check.getClass());
        return this;
    }

    @Override
    public ConformityTester withExcludedPackages(
            final String justification, final String... packageNames) {
        optionsSupporting(ExcludesPackages.class)
                .forEach(check -> check.withExcludedPackages(justification, packageNames));
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConformityTester withExcludedClasses(
            final String justification, final boolean excludeSubtypes, final Class<?>... classes) {
        optionsSupporting(ExcludesClasses.class)
                .forEach(
                        check ->
                                check.withExcludedClasses(justification, excludeSubtypes, classes));
        return this;
    }

    @Override
    public ConformityTester withExcludedClassPattern(
            final String justification, final Pattern pattern) {
        optionsSupporting(ExcludesClasses.class)
                .forEach(check -> check.withExcludedClassPattern(justification, pattern));
        return this;
    }

    @Override
    public ConformityTester withoutExcludedTestClassPattern(final String justification) {
        optionsSupporting(ExcludesClasses.class)
                .forEach(check -> check.withoutExcludedTestClassPattern(justification));
        return this;
    }

    @Override
    public void check() {
        try (CheckTarget ctx = new CheckTarget(typeFromModuleToTest)) {
            options.values().stream().map(this::runner).forEach(check -> invoke(check, ctx));
        }
    }

    private <T extends ConformityCheck> CheckRunner runner(final T options) {
        return runnerFactory(options).create(options);
    }

    @SuppressWarnings("unchecked")
    private <T extends ConformityCheck> CheckRunnerFactory<T> runnerFactory(final T options) {
        final CheckRunnerFactory<T> factory =
                (CheckRunnerFactory<T>) RUNNERS.get(options.getClass());
        if (factory == null) {
            throw new NoRunnerForCheckException(options);
        }
        return factory;
    }

    private <T extends ConformityCheck> void invoke(
            final CheckRunner check, final CheckTarget ctx) {
        try {
            check.check(ctx);
        } catch (final Exception e) {
            throw new ConformityCheckFailedError(check, e);
        }
    }

    private <T> Stream<T> optionsSupporting(final Class<T> type) {
        return options.values().stream()
                .filter(check -> type.isAssignableFrom(check.getClass()))
                .map(type::cast);
    }

    @FunctionalInterface
    private interface CheckRunnerFactory<T extends ConformityCheck> {
        CheckRunner create(T options);
    }

    private static final class ConformityCheckFailedError extends AssertionError {

        ConformityCheckFailedError(final CheckRunner check, final Throwable cause) {
            super(
                    "Conformity check failed. check: "
                            + check.name()
                            + ", reason: "
                            + cause.getMessage(),
                    cause);
        }
    }

    private static final class NoRunnerForCheckException extends IllegalStateException {

        NoRunnerForCheckException(final ConformityCheck options) {
            super("Unsupported check: " + options);
        }
    }
}
