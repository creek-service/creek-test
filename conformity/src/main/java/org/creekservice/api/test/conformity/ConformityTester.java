/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.test.conformity;

import org.creekservice.api.test.conformity.check.ConformityCheck;
import org.creekservice.internal.test.conformity.DefaultConformityTester;

/**
 * Check the conformity of Creek modules to a set of common rules.
 *
 * <p>See subtypes of {@link ConformityCheck} for details of checks.
 */
public interface ConformityTester
        extends ExcludesPackages<ConformityTester>, ExcludesClasses<ConformityTester> {

    /**
     * Execute the standard set of conformity checks against the module containing the supplied
     * {@code typeFromModuleToTest}.
     *
     * @param typeFromModuleToTest any type from the module to test.
     */
    static void test(Class<?> typeFromModuleToTest) {
        builder(typeFromModuleToTest).check();
    }

    /**
     * Get a builder to allow customisation of checks to run.
     *
     * @param typeFromModuleToTest any type from the module to test.
     * @return builder.
     */
    static ConformityTester builder(Class<?> typeFromModuleToTest) {
        return new DefaultConformityTester(typeFromModuleToTest);
    }

    /**
     * Customize a check.
     *
     * <p>For example:
     *
     * <pre>{@code
     * ConformityTester.builder(ModuleTest.class)
     *                 .withCustom(CheckExportedPackages.builder()
     *                 .excludedPackages("Excluded because ...", "some.package")
     *                 .check();
     * }</pre>
     *
     * @param check the customized check
     * @return self
     */
    ConformityTester withCustom(ConformityCheck check);

    /**
     * Disable a check.
     *
     * <p>For example:
     *
     * <pre>{@code
     * ConformityTester.builder(ModuleTest.class)
     *                 .withDisabled("Disabling because ...", CheckModule.builder())
     *                 .check();
     * }</pre>
     *
     * @param justification the reason why its being disabled.
     * @param check the check to disable
     * @return self.
     */
    ConformityTester withDisabled(String justification, ConformityCheck check);

    /**
     * Execute the checks
     *
     * @throws AssertionError if any checks fail.
     */
    void check();
}
