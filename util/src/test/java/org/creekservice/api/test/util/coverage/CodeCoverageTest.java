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

package org.creekservice.api.test.util.coverage;

import static org.creekservice.api.test.util.coverage.CodeCoverage.codeCoverageCmdLineArg;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.io.File;
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
                                "--javaagent: but not jacocoagent.jar"));

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
                                "--javaagent: but not jacocoagent.jar"));

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
                .thenReturn(List.of("--arg", "-javaagent:blah_jacocoagent.jar_blah", "-arg"));

        // When:
        final Optional<String> result = codeCoverageCmdLineArg(runtimeMXBean, Optional.empty());

        // Then:
        assertThat(result, is(Optional.of("-javaagent:blah_jacocoagent.jar_blah")));
    }

    @Test
    void shouldReturnJaCoCoAgentParamWithAbsolutePaths() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of("-javaagent:build/jacocoagent.jar:destfile=build/tmp/something"));

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
                                        + File.separator
                                        + "jacocoagent.jar:destfile="
                                        + abs
                                        + File.separator
                                        + "tmp/something")));
    }

    @Test
    void shouldReturnFirstJaCoCoAgentParam() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent:first_jacocoagent.jar",
                                "-javaagent:second_jacocoagent.jar"));

        // When:
        final Optional<String> result = codeCoverageCmdLineArg(runtimeMXBean, Optional.empty());

        // Then:
        assertThat(result, is(Optional.of("-javaagent:first_jacocoagent.jar")));
    }

    @Test
    void shouldWorkWithGradle7() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent:build/tmp/expandedArchives/org.jacoco.agent-0.8.8.jar"
                                        + "_a33b649e552c51298e5a242c2f0d0e3c/jacocoagent.jar="
                                        + "destfile=build/jacoco/test.exec,"
                                        + "append=true,inclnolocationclasses=false,dumponexit=true,output=file,jmx=false"));

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
                                        + File.separator
                                        + "tmp/expandedArchives/org.jacoco.agent-0.8.8.jar_a33b649e552c51298e5a242c2f0d0e3c/jacocoagent.jar="
                                        + "destfile="
                                        + abs
                                        + File.separator
                                        + "jacoco/test.exec,"
                                        + "append=true,inclnolocationclasses=false,dumponexit=true,output=file,jmx=false")));
    }

    @Test
    void shouldWorkWithGradle8() {
        // Given:
        when(runtimeMXBean.getInputArguments())
                .thenReturn(
                        List.of(
                                "-javaagent:/home/runner/work/creek-system-test/executor/build/tmp/"
                                        + ".cache/expanded/zip_a33b649e552c51298e5a242c2f0d0e3c/jacocoagent.jar="
                                        + "destfile=build/jacoco/test.exec,"
                                        + "append=true,inclnolocationclasses=false,dumponexit=true,output=file,jmx=false"));

        // When:
        final Optional<String> result =
                codeCoverageCmdLineArg(runtimeMXBean, Optional.of(BUILD_DIR));

        // Then:
        final Path abs = BUILD_DIR.toAbsolutePath();
        assertThat(
                result,
                is(
                        Optional.of(
                                "-javaagent:/home/runner/work/creek-system-test/executor/build/tmp/"
                                        + ".cache/expanded/zip_a33b649e552c51298e5a242c2f0d0e3c/jacocoagent.jar="
                                        + "destfile="
                                        + abs
                                        + File.separator
                                        + "jacoco/test.exec,"
                                        + "append=true,inclnolocationclasses=false,dumponexit=true,output=file,jmx=false")));
    }
}
