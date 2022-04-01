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
import org.creek.internal.test.conformity.check.DefaultCheckModule;

/**
 * Check that the module has a {@code module-info.java} file and the test itself is running with
 * modularity on.
 */
public interface CheckModule extends ConformityCheck.Builder {

    /** @return a builder used to customise the check */
    static CheckModule builder() {
        return new DefaultCheckModule.Builder();
    }
}
