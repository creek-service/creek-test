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

package org.creekservice.api.test.conformity;


import java.util.regex.Pattern;

/** Common interface for checks that exclude by class. */
public interface ExcludesClasses<T> {

    /**
     * Exclude one or more classes from the check
     *
     * @param justification text explaining why they are excluded.
     * @param classes classes to exclude. Subtypes will not be excluded.
     * @return self.
     */
    default T withExcludedClasses(String justification, Class<?>... classes) {
        return withExcludedClasses(justification, false, classes);
    }

    /**
     * Exclude one or more classes from the check
     *
     * @param justification text explaining why they are excluded.
     * @param excludeSubtypes {@code} true if subtypes of the supplied {@code classes} should also
     *     be excluded
     * @param classes classes to exclude.
     * @return self.
     */
    T withExcludedClasses(String justification, boolean excludeSubtypes, Class<?>... classes);

    /**
     * Exclude types from the check based on a regex pattern.
     *
     * <p>The supplied {@code pattern} is matched against the fully qualified class name. Those
     * matching the pattern will be excluded.
     *
     * @param justification text explaining why they are excluded.
     * @param pattern the pattern to match against.
     * @return self.
     */
    default T withExcludedClassPattern(String justification, String pattern) {
        return withExcludedClassPattern(justification, Pattern.compile(pattern));
    }

    /**
     * Exclude types from the check based on a regex pattern.
     *
     * <p>The supplied {@code pattern} is matched against the fully qualified class name. Those
     * matching the pattern will be excluded.
     *
     * @param justification text explaining why they are excluded.
     * @param pattern the pattern to match against.
     * @return self.
     */
    T withExcludedClassPattern(String justification, Pattern pattern);

    /**
     * Do not exclude types ending in {@code Test}, or nested with such a type, by default.
     *
     * <p>By default, the conformity checks ignore any type ending in `Test`. This is done to avoid
     * conformity checking any test classes that have been monkey patched into the module, or their
     * nested types. The pattern used is {@code .*Test(\$.*)?}.
     *
     * <p>Disable to ensure production types ending in {@code Test} are not inadvertently excluded.
     *
     * @param justification text explaining why they are excluded.
     * @return self
     */
    T withoutExcludedTestClassPattern(String justification);
}
