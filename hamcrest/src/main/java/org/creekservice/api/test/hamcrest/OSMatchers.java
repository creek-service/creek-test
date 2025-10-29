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

package org.creekservice.api.test.hamcrest;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.creekservice.api.test.util.OSCheck;
import org.hamcrest.Matcher;
import org.hamcrest.core.CombinableMatcher;

/** Cross-platform matcher for handling OS differences. */
public final class OSMatchers {

    private OSMatchers() {}

    /**
     * Check to use if running on Windows.
     *
     * @param matcher the check to run.
     * @return builder on which the non-windows check can be set.
     * @param <LHS> the type being matched.
     */
    public static <LHS> CombinableOsMatcher<LHS> onWindows(final Matcher<? super LHS> matcher) {
        return new CombinableOsMatcher<>(matcher);
    }

    public static final class CombinableOsMatcher<X> {
        private final Matcher<? super X> windows;

        CombinableOsMatcher(final Matcher<? super X> matcher) {
            this.windows = both(requireNonNull(matcher, "linux")).and(is(OSCheck.isWindows()));
        }

        /**
         * Check to use if not running on Windows, i.e. Mac / Linux.
         *
         * @param matcher the check to run.
         * @return the combined matcher.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public Matcher<X> onUnix(final Matcher<? super X> matcher) {
            final Matcher<? super X> unix =
                    both(requireNonNull(matcher, "matcher")).and(is(not(OSCheck.isWindows())));
            return new CombinableMatcher(unix).or(windows);
        }
    }
}
