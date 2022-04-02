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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.creek.api.base.annotation.VisibleForTesting;
import org.creek.api.test.conformity.check.CheckApiPackagesExposed;
import org.creek.api.test.conformity.check.CheckModule;
import org.creek.api.test.conformity.empty.missing.NotExported;
import org.junit.jupiter.api.Test;

class ConformityTesterTest {

    @Test
    void shouldPassIfEverythingIsOk() {
        ConformityTester.test(VisibleForTesting.class);
    }

    @Test
    void shouldDetectNotExported() {
        // Given:
        final ConformityTester tester = ConformityTester.builder(ConformityTester.class);

        // When:
        final Error e = assertThrows(AssertionError.class, tester::check);

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Conformity check failed. check: CheckApiPackagesExposed, reason: Some API packages are not exposed"));
        assertThat(e.getCause().getMessage(), startsWith("Some API packages are not exposed"));
    }

    @Test
    void shouldDetectUnnamedModule() {
        // Given:
        final ConformityTester tester = ConformityTester.builder(EqualsTester.class);

        // When:
        final Error e = assertThrows(AssertionError.class, tester::check);

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Conformity check failed. check: CheckModule, reason: The module is automatic"));
        assertThat(e.getCause().getMessage(), startsWith("The module is automatic"));
    }

    @Test
    void shouldCustomiseChecks() {
        // Given:
        final ConformityTester tester =
                ConformityTester.builder(ConformityTester.class)
                        .withCustom(
                                CheckApiPackagesExposed.builder()
                                        .excludedPackages(NotExported.class.getPackageName()));

        // When:
        tester.check();

        // Then: did not throw
    }

    @Test
    void shouldDisableChecks() {
        // Given:
        final ConformityTester tester =
                ConformityTester.builder(EqualsTester.class)
                        .withDisabled(CheckModule.builder(), "To allow testing!");

        // When:
        tester.check();

        // Then: did not throw
    }

    @Test
    void shouldThrownWhenDisablingIfNotJustification() {
        // Given:
        final ConformityTester tester = ConformityTester.builder(EqualsTester.class);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tester.withDisabled(CheckModule.builder(), " "));

        // Then:
        assertThat(e.getMessage(), startsWith("justification can not be blank"));
    }
}
