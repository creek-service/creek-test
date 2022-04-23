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

package org.creek.internal.test.conformity.check;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.lang.module.ModuleDescriptor;
import java.util.Arrays;
import java.util.Set;
import org.creek.api.test.conformity.test.types.bad.NotExported;
import org.creek.internal.test.conformity.CheckTarget;
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
        check = new ExportedPackagesCheck(new ExportedPackagesCheck.Options());

        when(ctx.moduleUnderTest()).thenReturn(moduleUnderTest);
        when(moduleUnderTest.isNamed()).thenReturn(true);
        when(moduleUnderTest.getName()).thenReturn("Bob");
        when(moduleUnderTest.getDescriptor()).thenReturn(descriptor);
    }

    @Test
    void shouldIgnoreNoneApiPackages() {
        // Given:
        givenPackages("org.creek.other", "not.org.creek");

        // When:
        check.check(ctx);

        // Then: passed
    }

    @Test
    void shouldPassIfAllApiPackagesAreExportedAndNonApiPackagesAreNot() {
        // Given:
        givenPackages("org.creek.api.a", "org.creek.api.b", "org.creek.internal.a");
        givenExportedPackages("org.creek.api.a", "org.creek.api.b");

        // When:
        check.check(ctx);

        // Then: passed
    }

    @Test
    void shouldThrowOnNonExportedApiPackages() {
        // Given:
        givenPackages("org.creek.api", "org.creek.api.a", "org.creek.api.b");
        givenExportedPackages("org.creek.api.a");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> check.check(ctx));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "API packages are not exposed in the module's module-info.java file. module=Bob, unexposed_packages=["
                                + System.lineSeparator()
                                + "\torg.creek.api"
                                + System.lineSeparator()
                                + "\torg.creek.api.b"
                                + System.lineSeparator()
                                + "]"));
    }

    @Test
    void shouldThrowOnNonApiPackageExported() {
        // Given:
        givenPackages("org.creek.internal", "org.creek.internal.a", "org.creek.internal.b");
        givenExportedPackages("org.creek.internal.a", "org.creek.internal.b");

        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> check.check(ctx));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "Non-API packages are exposed (without a 'to' clause) "
                                + "in the module's module-info.java file. module=Bob, exposed_packages=["
                                + System.lineSeparator()
                                + "\torg.creek.internal.a"
                                + System.lineSeparator()
                                + "\torg.creek.internal.b"
                                + System.lineSeparator()
                                + "]"));
    }

    @Test
    void shouldIgnoreUnnamedModules() {
        // Given:
        when(moduleUnderTest.isNamed()).thenReturn(false);
        when(moduleUnderTest.getDescriptor()).thenReturn(null);
        givenPackages("org.creek.api.a");

        // When:
        check.check(ctx);

        // Then: did not throw.
    }

    @Test
    void shouldIgnoreAutomaticModules() {
        // Given:
        when(descriptor.isAutomatic()).thenReturn(true);
        givenPackages("org.creek.api.a");

        // When:
        check.check(ctx);

        // Then: did not throw.
    }

    @Test
    void shouldIgnoreExcludedPackages() {
        // Given:
        givenPackages("org.creek.api.a", "org.creek.api.b.c");

        check =
                new ExportedPackagesCheck(
                        new ExportedPackagesCheck.Options()
                                .excludedPackages("org.creek.api.a", "org.creek.api.b.*"));

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

        // Then: `org.creek.api.test.conformity.test.types` is not in list as it contains no
        // classes:
        assertThat(
                e.getMessage(),
                containsString(
                        "["
                                + System.lineSeparator()
                                + "\torg.creek.api.test.conformity.test.types.bad"
                                + System.lineSeparator()
                                + "]"));
    }

    private void givenPackages(final String... packageNames) {
        when(moduleUnderTest.getPackages()).thenReturn(Set.of(packageNames));
    }

    private void givenExportedPackages(final String... packageNames) {
        Arrays.stream(packageNames)
                .forEach(pkg -> when(moduleUnderTest.isExported(pkg)).thenReturn(true));
    }
}
