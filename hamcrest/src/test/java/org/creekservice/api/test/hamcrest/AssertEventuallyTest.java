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

package org.creekservice.api.test.hamcrest;

import static java.time.Duration.ofMillis;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.creekservice.api.test.hamcrest.AssertEventually.RetryOnException;
import static org.creekservice.api.test.hamcrest.AssertEventually.assertThatEventually;
import static org.creekservice.api.test.hamcrest.AssertEventually.withSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.google.common.testing.NullPointerTester;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import org.creekservice.api.test.hamcrest.AssertEventually.ExceptionFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AssertEventuallyTest {

    @Mock private Supplier<Integer> supplier;
    private final long start = System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        when(supplier.get()).thenReturn(3);
    }

    @Test
    void shouldThrowNPEs() {
        new NullPointerTester()
                .setDefault(AssertEventually.Settings.class, withSettings())
                .testAllPublicStaticMethods(AssertEventually.class);

        new NullPointerTester().testAllPublicInstanceMethods(withSettings());
    }

    @Test
    void shouldPassEventually() {
        // Given:
        final Iterator<Integer> it = List.of(1, 2, 3, 4).iterator();

        // When:
        final Integer result = assertThatEventually(it::next, is(4));

        // Then:
        assertThat(result, is(4));
    }

    @Test
    void shouldTimeoutEventually() {
        // When:
        final AssertionError e =
                assertThrows(
                        AssertionError.class,
                        () ->
                                assertThatEventually(
                                        supplier, is(4), withSettings().withTimeout(ofMillis(1))));

        // Then:
        assertThat(e.getMessage(), containsString("Expected: is <4>"));
        assertThat(e.getMessage(), containsString("     but: was <3>"));
    }

    @Test
    void shouldAcceptMessage() {
        // When:
        final AssertionError e =
                assertThrows(
                        AssertionError.class,
                        () ->
                                assertThatEventually(
                                        supplier,
                                        is(4),
                                        withSettings()
                                                .withTimeout(ofMillis(1))
                                                .withMessage("hello")));

        // Then:
        assertThat(e.getMessage(), containsString("hello"));
        assertThat(e.getMessage(), containsString("Expected: is <4>"));
        assertThat(e.getMessage(), containsString("     but: was <3>"));
    }

    @Test
    void shouldAcceptMessageSupplier() {
        // When:
        final AssertionError e =
                assertThrows(
                        AssertionError.class,
                        () ->
                                assertThatEventually(
                                        supplier,
                                        is(4),
                                        withSettings()
                                                .withTimeout(ofMillis(1))
                                                .withMessage(() -> "hello")));

        // Then:
        assertThat(e.getMessage(), containsString("hello"));
        assertThat(e.getMessage(), containsString("Expected: is <4>"));
        assertThat(e.getMessage(), containsString("     but: was <3>"));
    }

    @Test
    void shouldFailOnException() {
        // Given:
        final RuntimeException expected = new RuntimeException("boom");
        when(supplier.get()).thenThrow(expected);

        // When:
        final RuntimeException e =
                assertThrows(RuntimeException.class, () -> assertThatEventually(supplier, is(4)));

        // Then:
        assertThat(e, is(sameInstance(expected)));
    }

    @Test
    void shouldRetryOnException() {
        // Given:
        when(supplier.get()).thenThrow(new RuntimeException("boom")).thenReturn(3, 4);

        // When:
        final Integer result =
                assertThatEventually(
                        supplier, is(4), withSettings().withExceptionFilter(RetryOnException));

        // Then:
        assertThat(result, is(4));
    }

    @Test
    void shouldAcceptCustomExceptionFilter() {
        // Given:
        when(supplier.get())
                .thenThrow(new ClassCastException("1"))
                .thenThrow(new RuntimeException("2"));

        final ExceptionFilter custom =
                e -> {
                    if (!(e instanceof ClassCastException)) {
                        throw e;
                    }
                };

        // When:
        final RuntimeException e =
                assertThrows(
                        RuntimeException.class,
                        () ->
                                assertThatEventually(
                                        supplier,
                                        is(4),
                                        withSettings().withExceptionFilter(custom)));

        // Then:
        assertThat(e.getMessage(), is("2"));
    }

    @Test
    void shouldAcceptTimeout() {
        // When:
        assertThrows(
                AssertionError.class,
                () ->
                        assertThatEventually(
                                supplier, is(4), withSettings().withTimeout(100, MILLISECONDS)));

        // Then:
        assertThat(System.currentTimeMillis() - start, is(greaterThanOrEqualTo(100L)));
    }

    @Test
    void shouldThrowOnInvalidInitialPeriod() {
        assertThrows(
                IllegalArgumentException.class,
                () -> withSettings().withInitialPeriod(0, MILLISECONDS));

        assertThrows(
                IllegalArgumentException.class,
                () -> withSettings().withInitialPeriod(-1, MILLISECONDS));
    }

    @Test
    void shouldAcceptInitialPeriod() {
        // When:
        assertThrows(
                AssertionError.class,
                () ->
                        assertThatEventually(
                                supplier,
                                is(4),
                                withSettings()
                                        .withInitialPeriod(10, MILLISECONDS)
                                        .withTimeout(40, MILLISECONDS)));

        // Then:
        final int times = Mockito.mockingDetails(supplier).getInvocations().size();
        assertThat(times, is(both(greaterThan(2)).and(lessThan(5))));
    }

    @Test
    void shouldAcceptMaxPeriod() {
        // When:
        assertThrows(
                AssertionError.class,
                () ->
                        assertThatEventually(
                                supplier,
                                is(4),
                                withSettings()
                                        .withMaxPeriod(1, MILLISECONDS)
                                        .withTimeout(30, MILLISECONDS)));

        // Then:
        final int times = Mockito.mockingDetails(supplier).getInvocations().size();
        assertThat(times, is(both(greaterThan(10)).and(lessThan(30))));
    }
}
