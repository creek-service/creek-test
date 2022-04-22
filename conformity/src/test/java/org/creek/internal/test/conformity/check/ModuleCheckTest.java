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
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.lang.module.ModuleDescriptor;
import java.net.URI;
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
class ModuleCheckTest {

    @Mock private CheckTarget ctx;
    @Mock private Module moduleUnderTest;
    @Mock private ModuleDescriptor descriptor;
    private CheckRunner check;

    @BeforeEach
    void setUp() {
        check = new ModuleCheck(new ModuleCheck.Options());

        when(ctx.moduleUnderTest()).thenReturn(moduleUnderTest);
        when(ctx.moduleLocation()).thenReturn(URI.create("file://path/to/module"));
        when(moduleUnderTest.getName()).thenReturn("Bob");
        when(moduleUnderTest.getDescriptor()).thenReturn(descriptor);
        when(moduleUnderTest.isNamed()).thenReturn(true);
        when(descriptor.isAutomatic()).thenReturn(false);
    }

    @Test
    void shouldPassIfNamedNonAutomaticModule() {
        // Given:
        when(moduleUnderTest.isNamed()).thenReturn(true);
        when(descriptor.isAutomatic()).thenReturn(false);

        // When:
        check.check(ctx);

        // Then: passed
    }

    @Test
    void shouldFailIfUnnamedModule() {
        // Given:
        when(moduleUnderTest.isNamed()).thenReturn(false);

        // When:
        final Exception e = assertThrows(Exception.class, () -> check.check(ctx));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "The module is not named. Missing module-info.java?"
                                + " module_name: unnamed"
                                + ", module_location: file://path/to/module"));
    }

    @Test
    void shouldFailIfAutomaticModule() {
        // Given:
        when(descriptor.isAutomatic()).thenReturn(true);

        // When:
        final Exception e = assertThrows(Exception.class, () -> check.check(ctx));

        // Then:
        assertThat(
                e.getMessage(),
                is(
                        "The module is automatic. Missing module-info.java?"
                                + " module_name: Bob"
                                + ", module_location: file://path/to/module"));
    }
}
