/*
 * Copyright 2022-2024 Creek Contributors (https://github.com/creek-service)
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

package org.creekservice.api.test.util.debug;

import static java.io.File.separator;
import static org.creekservice.api.test.util.debug.RemoteDebug.currentRemoteDebugArguments;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class RemoteDebugTest {

    @SetSystemProperty(key = "user.home", value = "/does.not.exist")
    @Test
    void shouldReturnEmptyIfNotJar() {
        // When:
        final List<String> args = RemoteDebug.remoteDebugArguments();

        // Then:
        assertThat(args, is(empty()));
    }

    @SetSystemProperty(key = "user.home", value = "src/test/resources/remote/debug/multiple")
    @Test
    void shouldPickLatestJar() {
        // When:
        final List<String> args = RemoteDebug.remoteDebugArguments();

        // Then:
        assertThat(args, hasSize(2));
        assertThat(args.get(0), containsString("agent-1.1.0.jar"));
        assertThat(args.get(0), not(containsString("agent-1.0.0.jar")));
    }

    @SetSystemProperty(key = "user.home", value = "src/test/resources/remote/debug/multiple")
    @Test
    void shouldReturnWithDefaultListenerPort() {
        // When:
        final List<String> args = RemoteDebug.remoteDebugArguments();

        // Then:
        assertThat(args, hasSize(2));
        final Path dir =
                Paths.get("src/test/resources/remote/debug/multiple/.attachme").toAbsolutePath();
        assertThat(
                args.get(0),
                is("-javaagent:" + dir + separator + "agent-1.1.0.jar=port:7857,host:localhost"));
        assertThat(
                args.get(1),
                is("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:0"));
    }

    @SetSystemProperty(key = "user.home", value = "src/test/resources/remote/debug/multiple")
    @Test
    void shouldReturnWithSpecificListenerPort() {
        // Given:
        final int listenerPort = 3654;

        // When:
        final List<String> args = RemoteDebug.remoteDebugArguments(listenerPort);

        // Then:
        assertThat(args, hasSize(2));
        assertThat(args.get(0), containsString("jar=port:" + listenerPort + ",host:"));
    }

    @SetSystemProperty(key = "user.home", value = "src/test/resources/remote/debug/multiple")
    @Test
    void shouldReturnForContainerWithDefaultListenerPort() {
        // Given:
        final int containerDebugPort = 84765;

        // When:
        final List<String> args = RemoteDebug.containerRemoteDebugArguments(containerDebugPort);

        // Then:
        assertThat(args, hasSize(2));
        final Path dir =
                Paths.get("src/test/resources/remote/debug/multiple/.attachme").toAbsolutePath();
        assertThat(
                args.get(0),
                is(
                        "-javaagent:"
                                + dir
                                + separator
                                + "agent-1.1.0.jar=port:7857,host:host.docker.internal"));
        assertThat(
                args.get(1),
                is(
                        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:"
                                + containerDebugPort));
    }

    @SetSystemProperty(key = "user.home", value = "src/test/resources/remote/debug/multiple")
    @Test
    void shouldReturnForContainerWithSpecificListenerPort() {
        // Given:
        final int listenerPort = 25975;
        final int containerDebugPort = 84765;

        // When:
        final List<String> args =
                RemoteDebug.containerRemoteDebugArguments(listenerPort, containerDebugPort);

        // Then:
        assertThat(args, hasSize(2));
        assertThat(args.get(0), containsString("port:" + listenerPort + ",host:"));
    }

    @Test
    void shouldNotFindDebugAgentInCurrentJvm() {
        assertThat((List<?>) currentRemoteDebugArguments(), is(empty()));
    }

    @Test
    void shouldReturnEmptyIfCurrentJvmHasNoAttachMeJavaAgent() {
        // Given:
        final List<String> inputs =
                List.of(
                        "an_arg",
                        "-agentlib:some.stuff-to-copy",
                        "another_arg",
                        "not-javaagent:blah.blah.attachme/attachme-agent-1.1.0.jar:blah",
                        "-javaagent:blah.blah.missing.attachme.stuff-1.1.0.jar:blah",
                        "blah");

        // When:
        final List<?> result = currentRemoteDebugArguments(inputs);

        // Then:
        assertThat(result, is(empty()));
    }

    @Test
    void shouldReturnEmptyIfCurrentJvmHasNoAttachMeAgentLib() {
        // Given:
        final List<String> inputs =
                List.of(
                        "an_arg",
                        "-not_agentlib:some.stuff-to-copy",
                        "not-agentlib:some.stuff-to-copy",
                        "another_arg",
                        "-javaagent:blah.blah.attachme/attachme-agent-1.1.0.jar:blah",
                        "blah");

        // When:
        final List<?> result = currentRemoteDebugArguments(inputs);

        // Then:
        assertThat(result, is(empty()));
    }

    @Test
    void shouldReturnArgsIfCurrentJvmHasRemoteDebuggingEnabled() {
        // Given:
        final List<String> inputs =
                List.of(
                        "an_arg",
                        "-agentlib:some.stuff-to-copy",
                        "another_arg",
                        "-javaagent:blah.blah.attachme/attachme-agent-1.1.0.jar:blah",
                        "blah");

        // When:
        final List<?> result = currentRemoteDebugArguments(inputs);

        // Then:
        assertThat(
                result,
                contains(
                        "-javaagent:blah.blah.attachme/attachme-agent-1.1.0.jar:blah",
                        "-agentlib:some.stuff-to-copy"));
    }
}
