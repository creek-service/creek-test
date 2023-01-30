/*
 * Copyright 2022-2023 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.test.util.coverage;

import static org.creekservice.api.test.util.coverage.CodeCoverage.codeCoverageCmdLineArg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.lang.management.RuntimeMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeCoverageTest {

    private static final Path BUILD_DIR = Paths.get("some/path/build/");
    @Mock private RuntimeMXBean runtimeMXBean;

    @Test
    void shouldEmptyIfNotJaCoCoAgentParamPresent() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent: but not Jacoco",
                                "--javaagent: but not org.jacoco.agent"));

        // When:
        final Optional<String> result = codeCoverageCmdLineArg(runtimeMXBean, Optional.empty());

        // Then:
        assertThat(result, is(Optional.empty()));
    }

    @Test
    void shouldEmptyIfNotJaCoCoAgentParamPresentAndBuildDirProvided() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent: but not Jacoco",
                                "--javaagent: but not org.jacoco.agent"));

        // When:
        final Optional<String> result =
                codeCoverageCmdLineArg(runtimeMXBean, Optional.of(BUILD_DIR));

        // Then:
        assertThat(result, is(Optional.empty()));
    }

    @Test
    void shouldReturnJaCoCoAgentParamAsIs() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(List.of("--arg", "-javaagent:blah_org.jacoco.agent_blah", "-arg"));

        // When:
        final Optional<String> result = codeCoverageCmdLineArg(runtimeMXBean, Optional.empty());

        // Then:
        assertThat(result, is(Optional.of("-javaagent:blah_org.jacoco.agent_blah")));
    }

    @Test
    void shouldReturnJaCoCoAgentParamWithAbsolutePaths() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent:build/org.jacoco.agent.jar:-destfile=build/tmp/something"));

        // When:
        final Optional<String> result =
                codeCoverageCmdLineArg(runtimeMXBean, Optional.of(BUILD_DIR));

        // Then:
        final Path abs = BUILD_DIR.toAbsolutePath();
        assertThat(
                result,
                is(
                        Optional.of(
                                "-javaagent:"
                                        + abs
                                        + "/org.jacoco.agent.jar:-destfile="
                                        + abs
                                        + "/tmp/something")));
    }

    @Test
    void shouldReturnFirstJaCoCoAgentParam() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent:first_org.jacoco.agent",
                                "-javaagent:second_org.jacoco.agent"));

        // When:
        final Optional<String> result = codeCoverageCmdLineArg(runtimeMXBean, Optional.empty());

        // Then:
        assertThat(result, is(Optional.of("-javaagent:first_org.jacoco.agent")));
    }
}
