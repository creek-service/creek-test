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

plugins {
    `java-library`
}

dependencies {
    testImplementation(project(":conformity"))
    // Depend on unnamed, which brings in bad types
    //   but that does not mean this module's conformity checks should fail.
    testImplementation(project(":test-unnamed"))
}

tasks.test {
    // As not a module, need to compliance check the actual jar:
    dependsOn("jar")
    classpath = files(tasks.jar.get().archiveFile, project.sourceSets.test.get().output, configurations.testRuntimeClasspath)
}
