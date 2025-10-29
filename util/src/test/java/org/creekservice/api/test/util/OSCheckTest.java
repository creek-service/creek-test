/*
 * Copyright 2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.test.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.either;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class OSCheckTest {

    @Test
    void shouldNotBlowUp() {
        assertThat(OSCheck.isWindows(), either(is(true)).or(is(false)));
    }

    @Test
    void shouldHandleNull() {
        assertFalse(OSCheck.isWindows(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Windows 10", "Windows 8.1", "Windows Server 2016"})
    void shouldDetectWindows(final String osName) {
        assertTrue(OSCheck.isWindows(osName));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Linux", "Unix"})
    void shouldDetectNonWindows(final String osName) {
        assertFalse(OSCheck.isWindows(osName));
    }
}
