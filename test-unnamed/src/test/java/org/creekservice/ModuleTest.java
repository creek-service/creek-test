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

package org.creekservice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.check.CheckConstructorsPrivate;
import org.creekservice.api.test.conformity.check.CheckModule;
import org.creekservice.api.unnamed.ApiTypeWithPublicConstructor;
import org.junit.jupiter.api.Test;

class ModuleTest {

    @Test
    void shouldBeUnnamed() {
        assertThat(ApiTypeWithPublicConstructor.class.getModule().isNamed(), is(false));
    }

    @Test
    void shouldFailOnBadTypes() {
        // Given:
        final ConformityTester tester =
                ConformityTester.builder(ApiTypeWithPublicConstructor.class)
                        .withDisabled("not a module", CheckModule.builder());

        // When:
        final Error e = assertThrows(AssertionError.class, tester::check);

        // Then:
        assertThat(e.getMessage(), containsString(ApiTypeWithPublicConstructor.class.getName()));
    }

    @Test
    void shouldFailIfNonModuleTypeNotFromJar() {
        // Given:
        final ConformityTester tester =
                ConformityTester.builder(ModuleTest.class)
                        .withDisabled("not a module", CheckModule.builder());

        // When:
        final Error e = assertThrows(AssertionError.class, tester::check);

        // Then:
        assertThat(
                e.getMessage(),
                containsString(
                        "Code location not a jar file. See:"
                            + " https://github.com/creek-service/creek-test/tree/main/conformity#testing-old-school-jars"));
    }

    @Test
    void shouldPassConformityFromUnnamedModule() {
        ConformityTester.builder(ApiTypeWithPublicConstructor.class)
                .withDisabled("not a module", CheckModule.builder())
                .withCustom(
                        CheckConstructorsPrivate.builder()
                                .withExcludedClasses(
                                        "deliberately bad type",
                                        ApiTypeWithPublicConstructor.class))
                .check();
    }
}
