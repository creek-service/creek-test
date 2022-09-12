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

import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Supplier;
import org.hamcrest.Matcher;

/** Hamcrest async assert with timeout. */
public final class AssertEventually {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_INITIAL_PAUSE_PERIOD = Duration.ofMillis(1);
    private static final Duration DEFAULT_MAX_PAUSE_PERIOD = Duration.ofSeconds(1);

    public interface ExceptionFilter {

        void accept(RuntimeException e);
    }

    public static final ExceptionFilter FailOnException =
            e -> {
                throw e;
            };

    public static final ExceptionFilter RetryOnException = e -> {};

    public static <T> T assertThatEventually(
            final Supplier<? extends T> actualSupplier, final Matcher<? super T> expected) {
        return assertThatEventually(actualSupplier, expected, FailOnException);
    }

    public static <T> T assertThatEventually(
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final ExceptionFilter exceptionFilter) {
        return assertThatEventually("", actualSupplier, expected, exceptionFilter);
    }

    public static <T> T assertThatEventually(
            final String message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected) {
        return assertThatEventually(() -> message, actualSupplier, expected);
    }

    public static <T> T assertThatEventually(
            final Supplier<String> message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected) {
        return assertThatEventually(message, actualSupplier, expected, FailOnException);
    }

    public static <T> T assertThatEventually(
            final String message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final ExceptionFilter exceptionFilter) {
        return assertThatEventually(() -> message, actualSupplier, expected, exceptionFilter);
    }

    public static <T> T assertThatEventually(
            final Supplier<String> message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final ExceptionFilter exceptionFilter) {
        return assertThatEventually(
                message, actualSupplier, expected, DEFAULT_TIMEOUT, exceptionFilter);
    }

    public static <T> T assertThatEventually(
            final String message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final Duration timeout) {
        return assertThatEventually(message, actualSupplier, expected, timeout, FailOnException);
    }

    public static <T> T assertThatEventually(
            final String message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final Duration timeout,
            final ExceptionFilter exceptionFilter) {
        return assertThatEventually(
                () -> message, actualSupplier, expected, timeout, exceptionFilter);
    }

    public static <T> T assertThatEventually(
            final Supplier<String> message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final Duration timeout,
            final ExceptionFilter exceptionFilter) {
        return assertThatEventually(
                message,
                actualSupplier,
                expected,
                timeout,
                DEFAULT_INITIAL_PAUSE_PERIOD,
                DEFAULT_MAX_PAUSE_PERIOD,
                exceptionFilter);
    }

    public static <T> T assertThatEventually(
            final String message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final Duration initialPausePeriod,
            final Duration maxPausePeriod) {
        return assertThatEventually(
                () -> message,
                actualSupplier,
                expected,
                DEFAULT_TIMEOUT,
                initialPausePeriod,
                maxPausePeriod);
    }

    public static <T> T assertThatEventually(
            final Supplier<String> message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final Duration timeout,
            final Duration initialPausePeriod,
            final Duration maxPausePeriod) {
        return assertThatEventually(
                message,
                actualSupplier,
                expected,
                timeout,
                initialPausePeriod,
                maxPausePeriod,
                FailOnException);
    }

    @SuppressWarnings("BusyWait")
    public static <T> T assertThatEventually(
            final Supplier<String> message,
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final Duration timeout,
            final Duration initialPausePeriod,
            final Duration maxPausePeriod,
            final ExceptionFilter exceptionFilter) {
        try {
            final Instant end = Instant.now().plus(timeout);

            long period = initialPausePeriod.toMillis();
            while (Instant.now().isBefore(end)) {
                T actual = null;
                boolean acquired = false;

                try {
                    actual = actualSupplier.get();
                    acquired = true;
                } catch (final RuntimeException e) {
                    exceptionFilter.accept(e);
                }

                if (acquired && expected.matches(actual)) {
                    return actual;
                }

                Thread.sleep(period);
                period = Math.min(period * 2, maxPausePeriod.toMillis());
            }

            final T actual = actualSupplier.get();
            assertThat(message.get(), actual, expected);
            return actual;
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private AssertEventually() {}
}
