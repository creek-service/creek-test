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

import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.hamcrest.Matcher;

/** Hamcrest async assert with timeout. */
public final class AssertEventually {

    @FunctionalInterface
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
        return assertThatEventually(actualSupplier, expected, withSettings());
    }

    @SuppressWarnings("BusyWait")
    public static <T> T assertThatEventually(
            final Supplier<? extends T> actualSupplier,
            final Matcher<? super T> expected,
            final Settings settings) {
        try {
            final Instant end = Instant.now().plus(settings.timeout);

            Duration period = settings.initialPeriod;
            while (Instant.now().isBefore(end)) {
                T actual = null;
                boolean acquired = false;

                try {
                    actual = actualSupplier.get();
                    acquired = true;
                } catch (final RuntimeException e) {
                    settings.exceptionFilter.accept(e);
                }

                if (acquired && expected.matches(actual)) {
                    return actual;
                }

                Thread.sleep(period.toMillis());

                period = increasePeriod(settings, period);
            }

            final T actual = actualSupplier.get();
            assertThat(settings.message.get(), actual, expected);
            return actual;
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static Settings withSettings() {
        return new Settings();
    }

    public static final class Settings {

        private Supplier<String> message = () -> "";
        private ExceptionFilter exceptionFilter = FailOnException;
        private Duration timeout = Duration.ofSeconds(30);
        private Duration initialPeriod = Duration.ofMillis(1);
        private Duration maxPeriod = Duration.ofSeconds(1);

        private Settings() {}

        public Settings withMessage(final String message) {
            requireNonNull(message, "message");
            this.message = () -> message;
            return this;
        }

        public Settings withMessage(final Supplier<String> message) {
            this.message = requireNonNull(message, "message");
            return this;
        }

        public Settings withExceptionFilter(final ExceptionFilter filter) {
            this.exceptionFilter = requireNonNull(filter, "filter");
            return this;
        }

        public Settings withTimeout(final Duration timeout) {
            this.timeout = requireNonNull(timeout, "timeout");
            return this;
        }

        public Settings withTimeout(final int count, final TimeUnit unit) {
            return withTimeout(Duration.of(count, unit.toChronoUnit()));
        }

        public Settings withInitialPeriod(final Duration period) {
            if (period.isZero() || period.isNegative()) {
                throw new IllegalArgumentException("period must be positive");
            }
            this.initialPeriod = requireNonNull(period, "period");
            return this;
        }

        public Settings withInitialPeriod(final int count, final TimeUnit unit) {
            return withInitialPeriod(Duration.of(count, unit.toChronoUnit()));
        }

        public Settings withMaxPeriod(final Duration period) {
            this.maxPeriod = requireNonNull(period, "period");
            return this;
        }

        public Settings withMaxPeriod(final int count, final TimeUnit unit) {
            return withMaxPeriod(Duration.of(count, unit.toChronoUnit()));
        }
    }

    private AssertEventually() {}

    private static Duration increasePeriod(final Settings settings, final Duration currentPeriod) {
        final Duration doubled = currentPeriod.multipliedBy(2);
        return doubled.compareTo(settings.maxPeriod) < 0 ? doubled : settings.maxPeriod;
    }
}
