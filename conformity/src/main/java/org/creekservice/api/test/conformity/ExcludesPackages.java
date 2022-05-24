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

/** Common interface for checks that exclude by package. */
public interface ExcludesPackages<T> {
    /**
     * Exclude classes in one or more packages from the check
     *
     * @param justification text explaining why they are excluded.
     * @param packageNames packages to exclude. Any name ending in `.*` will also ignore all
     *     sub-packages.
     * @return self.
     */
    T withExcludedPackages(String justification, String... packageNames);
}
