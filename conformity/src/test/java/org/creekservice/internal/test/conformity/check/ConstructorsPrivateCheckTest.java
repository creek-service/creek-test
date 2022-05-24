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

package org.creekservice.internal.test.conformity.check;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.creekservice.api.test.conformity.test.types.bad.ExampleTest;
import org.creekservice.api.test.conformity.test.types.bad.PublicSubTypeWithPublicConstructor;
import org.creekservice.api.test.conformity.test.types.bad.PublicTypeWithImplicitPublicConstructor;
import org.creekservice.api.test.conformity.test.types.bad.PublicTypeWithPublicConstructor;
import org.creekservice.internal.test.conformity.CheckTarget;
import org.creekservice.internal.test.conformity.check.ConstructorsPrivateCheck.Options;
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
        check = new ConstructorsPrivateCheck(new Options());
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
                        new Options()
                                .withExcludedPackages(
                                        "testing",
                                        PublicSubTypeWithPublicConstructor.class.getPackageName()));

        // When:
        check.check(target);

        // Then: did not fail.
    }

    @Test
    void shouldExcludeByType() {
        // Given:
        final Options options = new Options();
        options.withExcludedClasses(
                "testing",
                PublicTypeWithPublicConstructor.class,
                PublicTypeWithImplicitPublicConstructor.class,
                PublicSubTypeWithPublicConstructor.class);
        check = new ConstructorsPrivateCheck(options);

        // When:
        check.check(target);

        // Then: did not fail.
    }

    @Test
    void shouldExcludeBySubType() {
        // Given:
        check =
                new ConstructorsPrivateCheck(
                        new Options()
                                .withExcludedClasses(
                                        "testing",
                                        true,
                                        PublicTypeWithPublicConstructor.class,
                                        PublicTypeWithImplicitPublicConstructor.class));

        // When:
        check.check(target);

        // Then: did not fail.
    }

    @Test
    void shouldExcludeByClassPattern() {
        // Given:
        final Options options = new Options();
        options.withExcludedClassPattern(
                "testing", PublicTypeWithPublicConstructor.class.getPackageName() + "\\.Public.*");
        check = new ConstructorsPrivateCheck(options);

        // When:
        check.check(target);

        // Then: did not fail.
    }

    @Test
    void shouldAllowInclusionOfTestFiles() {
        // Given:
        final Options options = new Options();
        options.withExcludedClassPattern(
                        "testing",
                        PublicTypeWithPublicConstructor.class.getPackageName() + "\\.Public.*")
                .withoutExcludedTestClassPattern("testing");
        check = new ConstructorsPrivateCheck(options);

        final Exception e = assertThrows(RuntimeException.class, () -> check.check(target));

        // Then:
        assertThat(
                e.getMessage(),
                containsString(
                        ExampleTest.class.getName() + " has public constructors: public <init>()"));

        assertThat(
                e.getMessage(),
                containsString(
                        ExampleTest.NestedType.class.getName()
                                + " has public constructors: public <init>()"));
    }

    @Test
    void shouldThrownOnEmptyPackageJustification() {
        // Given:
        final Options options = new Options();

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> options.withExcludedPackages(" ", "org.creekservice.api.a"));

        // Then:
        assertThat(e.getMessage(), startsWith("justification can not be blank"));
    }

    @Test
    void shouldThrownOnEmptyClassJustification() {
        // Given:
        final Options options = new Options();

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> options.withExcludedClasses(" ", getClass()));

        // Then:
        assertThat(e.getMessage(), startsWith("justification can not be blank"));
    }

    @Test
    void shouldThrownOnEmptyClassPatternJustification() {
        // Given:
        final Options options = new Options();

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> options.withExcludedClassPattern(" ", ".*"));

        // Then:
        assertThat(e.getMessage(), startsWith("justification can not be blank"));
    }

    @Test
    void shouldThrownOnEmptyTestJustification() {
        // Given:
        final Options options = new Options();

        // When:
        final Exception e =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> options.withoutExcludedTestClassPattern("\t"));

        // Then:
        assertThat(e.getMessage(), startsWith("justification can not be blank"));
    }
}
