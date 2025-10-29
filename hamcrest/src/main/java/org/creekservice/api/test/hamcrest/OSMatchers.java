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

import org.creekservice.api.test.util.OSCheck;
import org.hamcrest.Matcher;

/** Cross-platform matcher for handling OS differences. */
public final class OSMatchers {

    private OSMatchers() {}

    /**
     * Check to use if running on Windows.
     *
     * @param windows the check to run on Windows systems.
     * @return builder on which the non-windows check can be set.
     * @param <LHS> the type being matched.
     */
    public static <LHS> SelectiveOsMatcher<LHS> onWindows(final Matcher<? super LHS> windows) {
        return new SelectiveOsMatcher<>(windows);
    }

    public static final class SelectiveOsMatcher<X> {
        private final Matcher<? super X> windows;

        SelectiveOsMatcher(final Matcher<? super X> matcher) {
            this.windows = requireNonNull(matcher, "linux");
        }

        /**
         * Check to use if not running on Windows, i.e. Mac / Linux.
         *
         * @param unix the check to run on *unix systems.
         * @return the combined matcher.
         */
        public Matcher<? super X> onUnix(final Matcher<? super X> unix) {
            return OSCheck.isWindows() ? windows : unix;
        }
    }
}
