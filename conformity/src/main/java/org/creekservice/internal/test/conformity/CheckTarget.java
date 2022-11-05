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
import java.util.concurrent.atomic.AtomicReference;

/** A target on which checks will be run. */
public final class CheckTarget implements AutoCloseable {

    private final URI location;
    private final Module moduleUnderTest;
    private final Class<?> typeFromModuleToTest;
    private final AtomicReference<ClassFinder> types;

    /**
     * Create instance
     *
     * @param typeFromModuleToTest any type from the module under test.
     */
    public CheckTarget(final Class<?> typeFromModuleToTest) {
        this.typeFromModuleToTest = requireNonNull(typeFromModuleToTest, "typeFromModuleToTest");
        this.location = location(typeFromModuleToTest);
        this.moduleUnderTest = typeFromModuleToTest.getModule();
        this.types = new AtomicReference<>();
    }

    /** @return the location of the module */
    public URI moduleLocation() {
        return location;
    }

    /** @return the module under test */
    public Module moduleUnderTest() {
        return moduleUnderTest;
    }

    /** @return the types the module contains */
    public ModuleTypes types() {
        return types.updateAndGet(
                existing -> existing == null ? new ClassFinder(typeFromModuleToTest) : existing);
    }

    @Override
    public void close() {
        types.updateAndGet(
                existing -> {
                    if (existing != null) {
                        existing.close();
                    }
                    return null;
                });
    }

    private static URI location(final Class<?> typeFromModuleToTest) {
        try {
            return typeFromModuleToTest.getProtectionDomain().getCodeSource().getLocation().toURI();
        } catch (final Exception e) {
            return URI.create("unknown://");
        }
    }
}
