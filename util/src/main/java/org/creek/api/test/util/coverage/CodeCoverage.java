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

package org.creek.api.test.util.coverage;


import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Optional;
import org.creek.api.base.annotation.VisibleForTesting;

public final class CodeCoverage {

    private CodeCoverage() {}

    /**
     * Finds the code coverage Java agent command line arg this JVM was started with, if any.
     *
     * <p>Useful to set on child processes started by tests to ensure the code they execute is
     * included in coverage metrics.
     *
     * @return the Java agent command line arg, or empty.
     */
    public static Optional<String> codeCoverageCmdLineArg() {
        return codeCoverageCmdLineArg(ManagementFactory.getRuntimeMXBean());
    }

    @VisibleForTesting
    static Optional<String> codeCoverageCmdLineArg(final RuntimeMXBean runtimeMXBean) {
        return runtimeMXBean.getInputArguments().stream()
                .filter(arg -> arg.startsWith("-javaagent"))
                .filter(arg -> arg.contains("org.jacoco.agent"))
                .reduce((first, second) -> first);
    }
}
