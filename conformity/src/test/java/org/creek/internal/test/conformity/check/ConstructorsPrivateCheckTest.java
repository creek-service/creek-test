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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.creek.api.test.conformity.test.types.bad.PublicSubTypeWithPublicConstructor;
import org.creek.api.test.conformity.test.types.bad.PublicTypeWithImplicitPublicConstructor;
import org.creek.api.test.conformity.test.types.bad.PublicTypeWithPublicConstructor;
import org.creek.internal.test.conformity.CheckTarget;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConstructorsPrivateCheckTest {

    private static CheckTarget target;
    private CheckRunner check;

    @BeforeAll
    static void beforeAll() {
        target = new CheckTarget(ConstructorsPrivateCheckTest.class);
    }

    @AfterAll
    static void afterAll() {
        target.close();
    }

    @BeforeEach
    void setUp() {
        check = new ConstructorsPrivateCheck(new ConstructorsPrivateCheck.Options());
    }

    @Test
    void shouldDetectPublicConstructor() {
        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> check.check(target));

        // Then:
        assertThat(
                e.getMessage(),
                startsWith(
                        "API types should not have public constructors."
                                + " Use factory methods instead."
                                + " module: creek.test.conformity,"
                                + " types:"));

        assertThat(
                e.getMessage(),
                containsString(
                        PublicTypeWithPublicConstructor.class.getName()
                                + " has public constructors: public <init>(int), public <init>(long)"));
    }

    @Test
    void shouldDetectImplicitPublicConstructor() {
        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> check.check(target));

        // Then:
        assertThat(
                e.getMessage(),
                containsString(
                        PublicTypeWithImplicitPublicConstructor.class.getName()
                                + " has public constructors: public <init>()"));
    }

    @Test
    void shouldDetectPublicConstructorsOnSubType() {
        // When:
        final Exception e = assertThrows(RuntimeException.class, () -> check.check(target));

        // Then:
        assertThat(
                e.getMessage(),
                containsString(
                        PublicSubTypeWithPublicConstructor.class.getName()
                                + " has public constructors: public <init>(int)"));
        assertThat(
                e.getMessage(),
                not(
                        containsString(
                                PublicSubTypeWithPublicConstructor.class.getName()
                                        + " has public constructors: public <init>(int), public <init>(long)")));
    }

    @Test
    void shouldExcludeByPackage() {
        // Given:
        check =
                new ConstructorsPrivateCheck(
                        new ConstructorsPrivateCheck.Options()
                                .excludedPackages(
                                        PublicSubTypeWithPublicConstructor.class.getPackageName()));

        // When:
        check.check(target);

        // Then: did not fail.
    }

    @Test
    void shouldExcludeByType() {
        // Given:
        check =
                new ConstructorsPrivateCheck(
                        new ConstructorsPrivateCheck.Options()
                                .excludedClasses(
                                        PublicTypeWithPublicConstructor.class,
                                        PublicTypeWithImplicitPublicConstructor.class,
                                        PublicSubTypeWithPublicConstructor.class));

        // When:
        check.check(target);

        // Then: did not fail.
    }

    @Test
    void shouldExcludeBySubType() {
        // Given:
        check =
                new ConstructorsPrivateCheck(
                        new ConstructorsPrivateCheck.Options()
                                .excludedClasses(
                                        true,
                                        PublicTypeWithPublicConstructor.class,
                                        PublicTypeWithImplicitPublicConstructor.class));

        // When:
        check.check(target);

        // Then: did not fail.
    }
}
