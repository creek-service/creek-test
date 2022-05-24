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

package org.creekservice;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.check.CheckModule;
import org.creekservice.api.unnamed.DifferentBadlyWrittenApiType;
import org.junit.jupiter.api.Test;

class ModuleTest {

    @Test
    void shouldBeUnnamed() {
        assertThat(DifferentBadlyWrittenApiType.class.getModule().isNamed(), is(false));
    }

    @Test
    void shouldFailOnBadTypes() {
        // Given:
        final ConformityTester tester =
                ConformityTester.builder(DifferentBadlyWrittenApiType.class)
                        .withDisabled("not a module", CheckModule.builder());

        // When:
        final Error e = assertThrows(AssertionError.class, tester::check);

        // Then:
        assertThat(e.getMessage(), containsString(DifferentBadlyWrittenApiType.class.getName()));
    }

    @Test
    void shouldPassConformityFromUnnamedModule() {
        // Importantly, this test should not fail due to types imported from test-unnamed:
        ConformityTester.builder(DifferentBadlyWrittenApiType.class)
                .withDisabled("not a module", CheckModule.builder())
                .withExcludedClasses("Intentionally bad", DifferentBadlyWrittenApiType.class)
                .check();
    }
}
