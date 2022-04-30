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

import static org.creek.api.test.util.coverage.CodeCoverage.codeCoverageCmdLineArg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CodeCoverageTest {

    @Mock private RuntimeMXBean runtimeMXBean;

    @Test
    void shouldEmptyIfNotJaCoCoAgentParamPresent() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent but not Jacoco",
                                "--javaagent but not org.jacoco.agent"));

        // When:
        final Optional<String> result = codeCoverageCmdLineArg(runtimeMXBean);

        // Then:
        assertThat(result, is(Optional.empty()));
    }

    @Test
    void shouldReturnJaCoCoAgentParam() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(List.of("--arg", "-javaagent_blah_org.jacoco.agent_blah", "-arg"));

        // When:
        final Optional<String> result = codeCoverageCmdLineArg(runtimeMXBean);

        // Then:
        assertThat(result, is(Optional.of("-javaagent_blah_org.jacoco.agent_blah")));
    }

    @Test
    void shouldReturnFirstJaCoCoAgentParam() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent_first_org.jacoco.agent",
                                "-javaagent_second_org.jacoco.agent"));

        // When:
        final Optional<String> result = codeCoverageCmdLineArg(runtimeMXBean);

        // Then:
        assertThat(result, is(Optional.of("-javaagent_first_org.jacoco.agent")));
    }
}
