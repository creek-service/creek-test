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

package org.creek.api.test.conformity.check;


import org.creek.api.test.conformity.ConformityCheck;
import org.creek.internal.test.conformity.check.DefaultCheckApiPackagesExposed;

/** All API packages should be exported in the {@code module-info.java} file. */
public interface CheckApiPackagesExposed extends ConformityCheck.Builder {

    /** @return a builder used to customise the check */
    static CheckApiPackagesExposed builder() {
        return new DefaultCheckApiPackagesExposed.Builder();
    }

    /**
     * Exclude one or more packages from the check
     *
     * @param packageNames packages to exclude. Any name ending in `.*` will ignore all sub-packages
     *     too.
     * @return self.
     */
    CheckApiPackagesExposed excludedPackages(String... packageNames);
}
