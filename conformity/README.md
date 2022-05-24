# Creek Test Conformity

Provides test helpers for checking Creek's own modules and code conform to certain standards.

## Adding conformity testing

Each module for conformity can be tested by adding a single test class utilising [`ConformityTester`][1]

```java
package org.creekservice;

import org.creekservice.api.test.conformity.ConformityTester;
import org.junit.jupiter.api.Test;

class ModuleTest {

    @Test
    void shouldConform() {
        ConformityTester.test(ModuleTest.class);
    }
}
```

For a list of checks, refer to the javadocs of [`ConformityCheck`][2] subtypes.

### Disabling checks:

Individual checks can be disabled. For example, if not testing a Java module you will need to disable the module test:

```java
class ModuleTest {
    @Test
    void shouldConform() {
        ConformityTester.builder(ModuleTest.class)
                .withDisabled(
                        "Does not run under JPMS",
                        CheckModule.builder())
                .check();
    }
}
```

### Customising checks

Some checks can be customised. See javadocs of [`ConformityCheck.Builder`][2] subtypes. For example:

```java
package org.creekservice;

import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.check.CheckExportedPackages;
import org.junit.jupiter.api.Test;

class ModuleTest {

    @Test
    void shouldConform() {
        ConformityTester.builder(ModuleTest.class)
                .withCustom(
                        CheckApiPackagesExposed.builder()
                                .excludedPackages(
                                        "justification on why they are excluded",
                                        "exact.package.to.exclude",
                                        "base.package.to.exclude.*"))
                .check();
    }
}
```

It is also possible to exclude types & packages across all checks that support such customisation. For example:

```java
package org.creekservice;

import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.check.CheckExportedPackages;
import org.junit.jupiter.api.Test;

class ModuleTest {

    @Test
    void shouldConform() {
        ConformityTester.builder(ModuleTest.class)
                .withExcludedPackages(
                        "justification on why they are excluded",
                        "package.a", "package.b.*")
                .withExcludedClasses(
                        "justification on why they are excluded",
                        Foo.class, Bar.class)
                .withExcludedClassPattern(
                        "justification on why they are excluded",
                        ".*Mock.*")
                .check();
    }
}
```

### Including test code

Any class ending in `Test`, or nested within such a class, is excluded by default. This is to avoid any test classes
that have been monkey patched into a module from causing conformity checks to fail.  However, it is possible to disable
this filter. This can be useful if a) the jar under test is not a Java Module, or b) there are production classes that
end with `Test` and a more specific filter is required.

```java
package org.creekservice;

import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.check.CheckExportedPackages;
import org.junit.jupiter.api.Test;

class ModuleTest {

    @Test
    void shouldConform() {
        ConformityTester.builder(ModuleTest.class)
                .withoutExcludedTestClassPattern("justification: as an example in the docs")
                .withExcludedClassPattern("test files", ".*ActualTest")
                .check();
    }
}
```

### Testing Old School Jars

The norm is to test Creek jars under JPMS as Java Modules. However, some jars, e.g. Gradle plugins, aren't.
Conformity testing outside JPMS requires a little extra setup to run the tests from the compiled jar, which allows
the tests to restrict the conformity testing to only the types in the jar, and not in dependencies.

Add the following to the project's `build.gradle.kts` to have the tests run with a compiled jar:

```kotlin
tasks.test {
    // As not a module, need to compliance check the actual jar:
    dependsOn("jar")
    classpath = files(tasks.jar.get().archiveFile, project.sourceSets.test.get().output, configurations.testRuntimeClasspath)
}
```

and ensure the type passed to the conformity tester is from the jar and not a test type:

```java
package org.creekservice;

import org.creekservice.api.test.conformity.ConformityTester;
import org.creekservice.api.test.conformity.check.CheckExportedPackages;
import org.junit.jupiter.api.Test;

class ModuleTest {
    @Test
    void shouldPassConformityFromUnnamedModule() {
        ConformityTester.builder(TypeFromTheProductionCode.class)
                .withDisabled("not a module", CheckModule.builder())
                .withoutExcludedTestClassPattern("not a module: test types are not in the jar")
                .check();
    }
}
```

[1]: src/main/java/org/creekservice/api/test/conformity/ConformityTester.java
[2]: src/main/java/org/creekservice/api/test/conformity/check/ConformityCheck.java