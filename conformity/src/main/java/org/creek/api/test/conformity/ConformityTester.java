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

package org.creek.api.test.conformity;

import static java.util.Objects.requireNonNull;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.creek.api.test.conformity.check.CheckExportedPackages;
import org.creek.api.test.conformity.check.CheckModule;
import org.creek.internal.test.conformity.DefaultCheckContext;

/**
 * Check the conformity of Creek modules to a set of common rules.
 *
 * <p>See impls of {@link ConformityCheck} for details of checks.
 */
public final class ConformityTester {

    private static final List<ConformityCheck.Builder> DEFAULT_CHECKS =
            List.of(CheckModule.builder(), CheckExportedPackages.builder());

    private final Class<?> typeFromModuleToTest;
    private final Map<Class<? extends ConformityCheck.Builder>, ConformityCheck.Builder> checks =
            new HashMap<>();

    /**
     * Execute the standard set of conformity checks against the module containing the supplied
     * {@code typeFromModuleToTest}.
     *
     * @param typeFromModuleToTest any type from the module to test.
     */
    public static void test(final Class<?> typeFromModuleToTest) {
        builder(typeFromModuleToTest).check();
    }

    /**
     * Get a builder to allow customisation of checks to run.
     *
     * @param typeFromModuleToTest any type from the module to test.
     * @return builder.
     */
    public static ConformityTester builder(final Class<?> typeFromModuleToTest) {
        return new ConformityTester(typeFromModuleToTest);
    }

    private ConformityTester(final Class<?> typeFromModuleToTest) {
        this.typeFromModuleToTest = requireNonNull(typeFromModuleToTest, "typeFromModuleToTest");

        DEFAULT_CHECKS.forEach(check -> checks.put(check.getClass(), check));
    }

    /**
     * Add a customised check.
     *
     * <p>This can be either an entirely new check, or a customised version of an existing check. In
     * the latter case, the existing check builder will be replaced. Detection of existing checks is
     * based on the {@link ConformityCheck.Builder builder type}.
     *
     * @param builder the check builder
     * @return self
     */
    public ConformityTester withCustom(final ConformityCheck.Builder builder) {
        this.checks.put(builder.getClass(), builder);
        return this;
    }

    /**
     * Disable one of the in-build checks.
     *
     * <p>For example:
     *
     * <pre>{@code
     * ConformityTester.builder(ModuleTest.class)
     *                 .withDisabled(CheckModule.builder(), "Not using modularity due to ...")
     *                 .check();
     * }</pre>
     *
     * @param builder the builder of the check to disable
     * @param justification the reason why its being disabled.
     * @return self.
     */
    public ConformityTester withDisabled(
            final ConformityCheck.Builder builder, final String justification) {
        if (justification.isBlank()) {
            throw new IllegalArgumentException("justification can not be blank.");
        }

        this.checks.remove(builder.getClass());
        return this;
    }

    /**
     * Execute the checks
     *
     * @throws AssertionError if any checks fail.
     */
    public void check() {
        final CheckTarget ctx =
                new DefaultCheckContext(
                        location(typeFromModuleToTest), typeFromModuleToTest.getModule());
        checks.values().forEach(check -> invoke(check.build(), ctx));
    }

    private void invoke(final ConformityCheck check, final CheckTarget ctx) {
        try {
            check.check(ctx);
        } catch (final Exception e) {
            throw new ConformityCheckFailedError(check, e);
        }
    }

    private static URI location(final Class<?> typeFromModuleToTest) {
        try {
            return typeFromModuleToTest.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (final Exception e) {
            return URI.create("unknown://");
        }
    }

    private static final class ConformityCheckFailedError extends AssertionError {

        ConformityCheckFailedError(final ConformityCheck check, final Throwable cause) {
            super(
                    "Conformity check failed. check: "
                            + check.name()
                            + ", reason: "
                            + cause.getMessage(),
                    cause);
        }
    }
}
