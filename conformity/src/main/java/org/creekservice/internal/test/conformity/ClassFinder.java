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

import static org.creekservice.internal.test.conformity.Constants.API_PACKAGE;
import static org.creekservice.internal.test.conformity.Constants.CREEK_PACKAGE;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.stream.Stream;

/** Finds Creek types in a module */
public final class ClassFinder implements ModuleTypes, AutoCloseable {

    private final ScanResult scanResult;

    /**
     * @param typeFromModuleToTest any type from the module to scan.
     */
    public ClassFinder(final Class<?> typeFromModuleToTest) {
        this.scanResult = scan(typeFromModuleToTest);
    }

    @Override
    public Stream<ClassInfo> classes() {
        return scanResult.getAllClasses().stream();
    }

    @Override
    public Stream<ClassInfo> apiClasses() {
        return classes().filter(ci -> ci.getPackageName().startsWith(API_PACKAGE));
    }

    @Override
    public void close() {
        scanResult.close();
    }

    private static ScanResult scan(final Class<?> typeFromModuleToTest) {
        final Module moduleUnderTest = typeFromModuleToTest.getModule();

        final String[] packages =
                moduleUnderTest.getPackages().stream()
                        .filter(pkg -> pkg.startsWith(CREEK_PACKAGE))
                        .toArray(String[]::new);

        final ClassGraph classGraph =
                new ClassGraph()
                        .enableClassInfo()
                        .enableMethodInfo()
                        .ignoreClassVisibility()
                        .acceptPackages(packages);

        if (moduleUnderTest.isNamed()) {
            classGraph.acceptModules(moduleUnderTest.getName());
        } else {
            classGraph.acceptJars(jarLeafName(typeFromModuleToTest));
        }

        return classGraph.scan();
    }

    private static String[] jarLeafName(final Class<?> typeFromModuleToTest) {
        try {
            final Path codeLocation =
                    Path.of(
                            typeFromModuleToTest
                                    .getProtectionDomain()
                                    .getCodeSource()
                                    .getLocation()
                                    .toURI());
            final Path fileName = codeLocation.getFileName();
            if (fileName == null || !fileName.toString().endsWith(".jar")) {
                throw new IllegalStateException(
                        "Code location not a jar file. "
                                + "See: https://github.com/creek-service/creek-test/tree/main/conformity#testing-old-school-jars");
            }

            return new String[] {fileName.toString()};
        } catch (URISyntaxException e) {
            return new String[] {};
        }
    }
}
