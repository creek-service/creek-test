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


import org.creek.internal.test.conformity.check.ConstructorsPrivateCheck;

/**
 * API classes should have private constructors and use factory methods for creation.
 *
 * <p>The use of factory methods makes it easier in the future to change the type of object
 * returned, allowing for more flexibility when refactoring.
 */
public interface CheckConstructorsPrivate extends ConformityCheck {

    /** @return a builder used to customise the check */
    static CheckConstructorsPrivate builder() {
        return new ConstructorsPrivateCheck.Options();
    }

    /**
     * Exclude one or more classes from the check
     *
     * @param classes classes to exclude. Subtypes will not be excluded.
     * @return self.
     */
    CheckConstructorsPrivate excludedClasses(Class<?>... classes);

    /**
     * Exclude one or more classes from the check
     *
     * @param excludeSubtypes {@code} true if subtypes of the supplied {@code classes} should also
     *     be excluded
     * @param classes classes to exclude.
     * @return self.
     */
    CheckConstructorsPrivate excludedClasses(boolean excludeSubtypes, Class<?>... classes);

    /**
     * Exclude classes in one or more packages from the check
     *
     * @param packageNames packages to exclude. Any name ending in `.*` will also ignore all
     *     sub-packages.
     * @return self.
     */
    CheckConstructorsPrivate excludedPackages(String... packageNames);
}
