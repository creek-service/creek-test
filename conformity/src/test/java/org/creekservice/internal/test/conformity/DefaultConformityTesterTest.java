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

package org.creekservice.internal.test.conformity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.google.common.testing.EqualsTester;
import org.creekservice.api.base.annotation.VisibleForTesting;
import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.check.CheckExportedPackages;
import org.creekservice.api.test.conformity.check.CheckModule;
import org.creekservice.api.test.conformity.check.ConformityCheck;
import org.creekservice.api.test.conformity.test.types.bad.NotExported;
import org.junit.jupiter.api.Test;

class DefaultConformityTesterTest {

    @Test
    void shouldPassIfEverythingIsOk() {
        ConformityTester.test(VisibleForTesting.class);
    }

    @Test
    void shouldDetectNotExported() {
        // Given:
        final ConformityTester tester =
                ConformityTester.builder(ConformityTester.class)
                        .withDisabled("Not testing this one", CheckModule.builder());

        // When:
        final Error e = assertThrows(AssertionError.class, tester::check);

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "Conformity check failed. check: CheckExportedPackages, reason: API packages are not exposed"));
        assertThat(e.getCause().getMessage(), startsWith("API packages are not exposed"));
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
    }

    @Test
    void shouldCustomiseChecks() {
        // Given:
        final ConformityTester tester =
                ConformityTester.builder(ConformityTester.class)
                        .withCustom(
                                "to test customising",
                                CheckExportedPackages.builder()
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
                        .withDisabled("To allow testing!", CheckModule.builder());

        // When:
        tester.check();

        // Then: did not throw
    }

    @Test
    void shouldThrownOnUnknownCheck() {
        // Given:
        final ConformityTester tester = ConformityTester.builder(EqualsTester.class);
        final ConformityCheck check = mock(ConformityCheck.class);

        // When:
        final Exception e =
                assertThrows(IllegalStateException.class, () -> tester.withCustom("cos", check));

        // Then:
        assertThat(e.getMessage(), startsWith("Unsupported check"));
    }

    @Test
    void shouldThrownWhenCustomisingIfNoJustification() {
        // Given:
        final ConformityTester tester = ConformityTester.builder(EqualsTester.class);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tester.withCustom(" ", CheckModule.builder()));

        // Then:
        assertThat(e.getMessage(), startsWith("justification can not be blank"));
    }

    @Test
    void shouldThrownWhenDisablingIfNoJustification() {
        // Given:
        final ConformityTester tester = ConformityTester.builder(EqualsTester.class);

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> tester.withDisabled(" ", CheckModule.builder()));

        // Then:
        assertThat(e.getMessage(), startsWith("justification can not be blank"));
    }
}
