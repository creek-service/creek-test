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

package org.creek.internal.test.conformity.check;


import java.net.URI;
import org.creek.api.test.conformity.CheckTarget;
import org.creek.api.test.conformity.ConformityCheck;
import org.creek.api.test.conformity.check.CheckModule;

/**
 * Check that the module has a {@code module-info.java} file and the test itself is running with
 * modularity on.
 */
public final class DefaultCheckModule implements ConformityCheck {

    private DefaultCheckModule() {}

    @Override
    public String name() {
        return CheckModule.class.getSimpleName();
    }

    @Override
    public void check(final CheckTarget target) {
        if (!target.moduleUnderTest().isNamed()) {
            throw new ModuleCheckException(
                    "The module is not named", "unnamed", target.moduleLocation());
        }

        if (target.moduleUnderTest().getDescriptor().isAutomatic()) {
            throw new ModuleCheckException(
                    "The module is automatic",
                    target.moduleUnderTest().getName(),
                    target.moduleLocation());
        }
    }

    public static final class Builder implements CheckModule {

        @Override
        public ConformityCheck build() {
            return new DefaultCheckModule();
        }
    }

    private static final class ModuleCheckException extends RuntimeException {

        ModuleCheckException(final String msg, final String moduleName, final URI moduleLocation) {
            super(
                    msg
                            + ". Missing module-info.java? module_name: "
                            + moduleName
                            + ", module_location: "
                            + moduleLocation);
        }
    }
}
