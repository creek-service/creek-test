/*
 * Copyright 2022-2025 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.test.conformity.test.types.bad;

/**
 * Under JPMS test code can be monkey patched into the main jar to avoid multiple jars exporting the
 * same package, i.e. split package. This means the conformity checks can run into test classes.
 * Such test classes should ideally not cause issue...
 */
@SuppressWarnings("unused") // Used to test checks.
public class ExampleTest {

    public static class NestedType {}
}
