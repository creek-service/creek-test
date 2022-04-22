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

package org.creek.internal.test.conformity;


import io.github.classgraph.ClassInfo;
import java.util.stream.Stream;

/** Information about the types in a module */
public interface ModuleTypes {

    /** @return all classes in the module */
    Stream<ClassInfo> classes();

    /** @return all api classes in the module. */
    Stream<ClassInfo> apiClasses();
}
