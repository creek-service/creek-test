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

package org.creekservice.internal.test.conformity;

import static java.util.Objects.requireNonNull;

import java.net.URI;

public final class CheckTarget {

    private final URI location;
    private final Module moduleUnderTest;

    public CheckTarget(final URI location, final Module moduleUnderTest) {
        this.location = requireNonNull(location, "location");
        this.moduleUnderTest = requireNonNull(moduleUnderTest, "moduleUnderTest");
    }

    public URI moduleLocation() {
        return location;
    }

    public Module moduleUnderTest() {
        return moduleUnderTest;
    }
}
