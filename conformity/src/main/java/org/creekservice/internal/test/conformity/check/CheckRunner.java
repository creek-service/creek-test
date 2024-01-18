/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.internal.test.conformity.check;

import org.creekservice.internal.test.conformity.CheckTarget;

/** Runner of a single check */
public interface CheckRunner {

    /**
     * @return the name of the check, used in error messages.
     */
    String name();

    /**
     * Run the check
     *
     * @param target check target.
     */
    void check(CheckTarget target);
}
