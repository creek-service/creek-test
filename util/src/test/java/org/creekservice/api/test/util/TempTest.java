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

package org.creekservice.api.test.util;

import static org.creekservice.api.test.hamcrest.PathMatchers.directory;
import static org.creekservice.api.test.hamcrest.PathMatchers.doesNotExist;
import static org.creekservice.api.test.util.Temp.recursiveDelete;
import static org.creekservice.api.test.util.TestPaths.write;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TempTest {

    @Mock private Temp.FileDeleteMethod deleteMethod;

    @Test
    void shouldGetTempDirectory() {
        // When:
        final Path path = Temp.tempDir("some-prefix");

        // Then:
        assertThat(path.getFileName().toString(), startsWith("some-prefix"));
        assertThat(path, is(directory()));
    }

    @Test
    void shouldThrowIfFailsToCreateDirectory() {
        // When:
        assertThrows(RuntimeException.class, () -> Temp.tempDir("/\\"));
    }

    @Test
    void shouldDeleteContents() throws Exception {
        // Given:
        final Path dir = Files.createTempDirectory("r");
        write(dir.resolve("file0"), "text");
        write(dir.resolve("sub0").resolve("file1"), "text");
        write(dir.resolve("sub1").resolve("file3"), "text");

        // When:
        recursiveDelete(dir, Files::delete);

        // Then:
        assertThat(dir, is(doesNotExist()));
    }

    @Test
    void shouldSwallowFailuresToDeleteFile() throws Exception {
        // Given:
        final Path dir = Temp.tempDir("some-prefix");
        final Path file = dir.resolve("file0");
        write(file, "text");

        doThrow(new IOException("BOOM")).when(deleteMethod).delete(file);

        // When:
        recursiveDelete(dir, deleteMethod);

        // Then:
        assertThat(dir, is(directory()));
    }

    @Test
    void shouldSwallowFailuresToDeleteDir() throws Exception {
        // Given:
        final Path dir = Temp.tempDir("some-prefix");
        write(dir.resolve("file0"), "text");

        doNothing().when(deleteMethod).delete(any());
        doThrow(new IOException("BOOM")).when(deleteMethod).delete(dir);

        // When:
        recursiveDelete(dir, deleteMethod);

        // Then:
        assertThat(dir, is(directory()));
    }
}
