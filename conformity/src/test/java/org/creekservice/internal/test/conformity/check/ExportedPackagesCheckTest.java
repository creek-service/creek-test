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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.lang.module.ModuleDescriptor;
import java.util.Arrays;
import java.util.Set;
import org.creekservice.api.test.conformity.test.types.bad.NotExported;
import org.creekservice.internal.test.conformity.CheckTarget;
import org.creekservice.internal.test.conformity.check.ExportedPackagesCheck.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExportedPackagesCheckTest {

    @Mock private CheckTarget ctx;
    @Mock private Module moduleUnderTest;
    @Mock private ModuleDescriptor descriptor;
    private CheckRunner check;

    @BeforeEach
    void setUp() {
        check = new ExportedPackagesCheck(new Options());

        when(ctx.moduleUnderTest()).thenReturn(moduleUnderTest);
        when(moduleUnderTest.isNamed()).thenReturn(true);
        when(moduleUnderTest.getName()).thenReturn("Bob");
        when(moduleUnderTest.getDescriptor()).thenReturn(descriptor);
    }

    @Test
    void shouldIgnoreNoneApiPackages() {
        // Given:
        givenPackages("org.creekservice.other", "not.org.creekservice");

        // When:
        check.check(ctx);

        // Then: passed
    }

    @Test
    void shouldPassIfAllApiPackagesAreExportedAndNonApiPackagesAreNot() {
        // Given:
        givenPackages(
                "org.creekservice.api.a", "org.creekservice.api.b", "org.creekservice.internal.a");
        givenExportedPackages("org.creekservice.api.a", "org.creekservice.api.b");

        // When:
        check.check(ctx);

        // Then: passed
    }

    @Test
    void shouldThrowOnNonExportedApiPackages() {
        // Given:
        givenPackages("org.creekservice.api", "org.creekservice.api.a", "org.creekservice.api.b");
        givenExportedPackages("org.creekservice.api.a");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> check.check(ctx));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "API packages are not exposed in the module's module-info.java file. module=Bob, unexposed_packages=["
                                + System.lineSeparator()
                                + "\torg.creekservice.api"
                                + System.lineSeparator()
                                + "\torg.creekservice.api.b"
                                + System.lineSeparator()
                                + "]"));
    }

    @Test
    void shouldThrowOnNonApiPackageExported() {
        // Given:
        givenPackages(
                "org.creekservice.internal",
                "org.creekservice.internal.a",
                "org.creekservice.internal.b");
        givenExportedPackages("org.creekservice.internal.a", "org.creekservice.internal.b");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> check.check(ctx));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Non-API packages are exposed (without a 'to' clause) "
                                + "in the module's module-info.java file. module=Bob, exposed_packages=["
                                + System.lineSeparator()
                                + "\torg.creekservice.internal.a"
                                + System.lineSeparator()
                                + "\torg.creekservice.internal.b"
                                + System.lineSeparator()
                                + "]"));
    }

    @Test
    void shouldIgnoreUnnamedModules() {
        // Given:
        when(moduleUnderTest.isNamed()).thenReturn(false);
        when(moduleUnderTest.getDescriptor()).thenReturn(null);
        givenPackages("org.creekservice.api.a");

        // When:
        check.check(ctx);

        // Then: did not throw.
    }

    @Test
    void shouldIgnoreAutomaticModules() {
        // Given:
        when(descriptor.isAutomatic()).thenReturn(true);
        givenPackages("org.creekservice.api.a");

        // When:
        check.check(ctx);

        // Then: did not throw.
    }

    @Test
    void shouldIgnoreExcludedPackages() {
        // Given:
        givenPackages("org.creekservice.api.a", "org.creekservice.api.b.c");

        check =
                new ExportedPackagesCheck(
                        new Options()
                                .withExcludedPackages(
                                        "testing",
                                        "org.creekservice.api.a",
                                        "org.creekservice.api.b.*"));

        // When:
        check.check(ctx);

        // Then: passed
    }

    @Test
    void shouldIgnoreEmptyPackages() {
        // Given:
        when(ctx.moduleUnderTest()).thenReturn(NotExported.class.getModule());

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> check.check(ctx));

        // Then: `org.creekservice.api.test.conformity.test.types` is not in list as it contains no
        // classes:
        assertThat(
                e.getMessage(),
                containsString(
                        "["
                                + System.lineSeparator()
                                + "\torg.creekservice.api.test.conformity.test.types.bad"
                                + System.lineSeparator()
                                + "]"));
    }

    @Test
    void shouldThrownOnEmptyJustification() {
        // Given:
        final Options options = new Options();

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> options.withExcludedPackages(" ", "org.creekservice.api.a"));

        // Then:
        assertThat(e.getMessage(), startsWith("justification can not be blank"));
    }

    private void givenPackages(final String... packageNames) {
        when(moduleUnderTest.getPackages()).thenReturn(Set.of(packageNames));
    }

    private void givenExportedPackages(final String... packageNames) {
        Arrays.stream(packageNames)
                .forEach(pkg -> when(moduleUnderTest.isExported(pkg)).thenReturn(true));
    }
}
