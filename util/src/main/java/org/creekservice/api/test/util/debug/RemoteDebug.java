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

package org.creekservice.api.test.util.debug;


import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Util class for discovering the command line arguments needed to debug remote processes.
 *
 * <p>Leverages the {@code Attach Me} IntellJ plugin.
 * https://blog.jetbrains.com/scala/2020/01/14/attachme-attach-the-intellij-idea-debugger-to-forked-jvms-automatically/
 */
public final class RemoteDebug {

    private static final int DEFAULT_LISTENER_PORT = 7857;

    private static final PathMatcher JAR_MATCHER =
            FileSystems.getDefault().getPathMatcher("glob:**.jar");

    private RemoteDebug() {}

    /**
     * Create JVM command line arguments for enabling remote debugging via Attach Me.
     *
     * <p>Searches the user home directory for the attachme jar. Ensure the attachme plugin has been
     * run first, as this automatically downloads the jar.
     *
     * @return the required command line arguments if the jar was found, otherwise an empty list.
     */
    public static List<String> remoteDebugArguments() {
        return remoteDebugArguments(DEFAULT_LISTENER_PORT);
    }

    /**
     * Create JVM command line arguments for enabling remote debugging via Attach Me.
     *
     * <p>Searches the user home directory for the attachme jar. Ensure the attachme plugin has been
     * run first, as this automatically downloads the jar.
     *
     * @param listenerPort the port the attachme plugin is listening on.
     * @return the required command line arguments if the jar was found, otherwise an empty list.
     */
    public static List<String> remoteDebugArguments(final int listenerPort) {
        return debugArguments(listenerPort, "localhost", 0);
    }

    /**
     * Create JVM command line arguments for enabling remote debugging via Attach Me.
     *
     * <p>Searches the user home directory for the attachme jar. Ensure the attachme plugin has been
     * run first, as this automatically downloads the jar.
     *
     * @param containerDebugPort the port mapped into the container, i.e. the internal port within
     *     the running container that maps back to the listening port.
     * @return the required command line arguments if the jar was found, otherwise an empty list.
     */
    public static List<String> containerRemoteDebugArguments(final int containerDebugPort) {
        return containerRemoteDebugArguments(DEFAULT_LISTENER_PORT, containerDebugPort);
    }

    /**
     * Create JVM command line arguments for enabling remote debugging via Attach Me.
     *
     * <p>Searches the user home directory for the attachme jar. Ensure the attachme plugin has been
     * run first, as this automatically downloads the jar.
     *
     * @param listenerPort the port the attachme plugin is listening on.
     * @param containerDebugPort the port mapped into the container, i.e. the internal port within
     *     the running container that maps back to the listening port.
     * @return the required command line arguments if the jar was found, otherwise an empty list.
     */
    public static List<String> containerRemoteDebugArguments(
            final int listenerPort, final int containerDebugPort) {
        return debugArguments(listenerPort, "host.docker.internal", containerDebugPort);
    }

    /**
     * Extract the JVM arguments for remote debugging from the current JVM.
     *
     * <p>This can be useful when wanting to pass remote debugging arguments through a chain of processes.
     *
     * @return the required command line arguments if present, otherwise an empty list.
     */
    public static List<String> currentRemoteDebugArguments() {
        return currentRemoteDebugArguments(
                ManagementFactory.getRuntimeMXBean().getInputArguments());
    }

    // VisibleForTesting
    static List<String> currentRemoteDebugArguments(final List<String> inputArguments) {
        return inputArguments.stream()
                .filter(arg -> arg.startsWith("-javaagent:"))
                .filter(arg -> arg.contains(".attachme/attachme-agent-"))
                .reduce((first1, second1) -> first1)
                .map(
                        javaAgent ->
                                inputArguments.stream()
                                        .filter(arg -> arg.startsWith("-agentlib:"))
                                        .reduce((first, second) -> first)
                                        .map(agentLib -> List.of(javaAgent, agentLib))
                                        .orElse(List.of()))
                .orElse(List.of());
    }

    private static List<String> debugArguments(
            final int listenerPort, final String hostName, final int clientPort) {
        return findAttacheMeJar()
                .map(
                        jar ->
                                List.of(
                                        "-javaagent:"
                                                + jar
                                                + "=port:"
                                                + listenerPort
                                                + ",host:"
                                                + hostName,
                                        "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:"
                                                + clientPort))
                .orElse(List.of());
    }

    @SuppressFBWarnings("RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE")
    private static Optional<Path> findAttacheMeJar() {
        final Path dir = Paths.get(System.getProperty("user.home")).resolve(".attachme");
        if (Files.notExists(dir)) {
            return Optional.empty();
        }

        try (Stream<Path> stream = Files.walk(dir, 1)) {
            return stream.filter(Files::isRegularFile)
                    .filter(JAR_MATCHER::matches)
                    .map(Path::toAbsolutePath)
                    .sorted()
                    .reduce((l, r) -> r);
        } catch (final IOException e) {
            return Optional.empty();
        }
    }
}
