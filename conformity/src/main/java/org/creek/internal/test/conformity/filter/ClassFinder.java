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

package org.creek.internal.test.conformity.filter;

import static java.util.Objects.requireNonNull;
import static org.creek.internal.test.conformity.Constants.API_PACKAGE;
import static org.creek.internal.test.conformity.Constants.CREEK_PACKAGE;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.util.stream.Stream;
import org.creek.internal.test.conformity.ModuleTypes;

public final class ClassFinder implements ModuleTypes, AutoCloseable {

    private final Module moduleUnderTest;
    private final ScanResult scanResult;

    public ClassFinder(final Module moduleUnderTest) {
        this.moduleUnderTest = requireNonNull(moduleUnderTest, "moduleUnderTest");
        this.scanResult =
                new ClassGraph()
                        .enableClassInfo()
                        .enableMethodInfo()
                        .ignoreClassVisibility()
                        .acceptPackages(CREEK_PACKAGE)
                        .scan();
    }

    @Override
    public Stream<ClassInfo> classes() {
        return scanResult.getAllClassesAsMap().values().stream()
                .filter(ci -> ci.getModuleInfo() != null)
                .filter(ci -> moduleUnderTest.getName().equals(ci.getModuleInfo().getName()));
    }

    @Override
    public Stream<ClassInfo> apiClasses() {
        return classes().filter(ci -> ci.getPackageName().startsWith(API_PACKAGE));
    }

    @Override
    public void close() {
        scanResult.close();
    }
}
