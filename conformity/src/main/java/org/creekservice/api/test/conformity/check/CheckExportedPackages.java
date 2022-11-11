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

package org.creekservice.api.test.conformity.check;


import org.creekservice.api.test.conformity.ExcludesPackages;
import org.creekservice.internal.test.conformity.check.ExportedPackagesCheck;

/**
 * All API packages should be exported to all modules, and no non-API packages should be exported to
 * all modules, in the {@code module-info.java} file.
 *
 * <p>Note, non-API packages can be exported <i>to</i> specific modules, e.g. other Creek modules.
 */
public interface CheckExportedPackages
        extends ConformityCheck, ExcludesPackages<CheckExportedPackages> {

    /**
     * @return a builder used to customise the check
     */
    static CheckExportedPackages builder() {
        return new ExportedPackagesCheck.Options();
    }
}
