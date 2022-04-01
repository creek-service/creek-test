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
import org.creek.api.test.conformity.check.CheckApiPackagesExposed;
import org.creek.api.test.conformity.check.CheckModule;
import org.creek.internal.test.conformity.DefaultCheckContext;

/**
 * Check the conformity of Creek modules to a set of common rules.
 *
 * <p>See impls of {@link ConformityCheck} for details of checks.
 */
public final class ConformityTester {

    private static final List<ConformityCheck> DEFAULT_CHECKS =
            List.of(CheckModule.builder().build(), CheckApiPackagesExposed.builder().build());

    private final Class<?> typeFromModuleToTest;
    private final Map<Class<? extends ConformityCheck>, ConformityCheck> checks = new HashMap<>();

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
    }

    /**
     * Add a customised check.
     *
     * <p>This can be either an entirely new check, or a customised version of an existing check. In
     * the latter case, the existing check will be replaced.
     *
     * @param builder the check builder
     * @return self
     */
    public ConformityTester withCustom(final ConformityCheck.Builder builder) {
        return withCustom(builder.build());
    }

    /**
     * Add a customised check.
     *
     * <p>This can be either an entirely new check, or a customised version of an existing check. In
     * the latter case, the existing check will be replaced.
     *
     * @param check the check
     * @return self
     */
    public ConformityTester withCustom(final ConformityCheck check) {
        this.checks.put(requireNonNull(check).getClass(), check);
        return this;
    }

    /**
     * Execute the checks
     *
     * @throws AssertionError if any checks fail.
     */
    public void check() {
        DEFAULT_CHECKS.forEach(check -> checks.putIfAbsent(check.getClass(), check));

        final CheckTarget ctx =
                new DefaultCheckContext(
                        location(typeFromModuleToTest), typeFromModuleToTest.getModule());
        checks.values().forEach(check -> invoke(check, ctx));
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
