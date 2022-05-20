# Creek Test Conformity

Provides test helpers for checking Creek's own modules and code conform to certain standards.

## Conformity testing

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
                                .excludedPackages("some.package.name.to.exclude.*"))
                .check();
    }
}
```

[1]: src/main/java/org/creekservice/api/test/conformity/ConformityTester.java
[2]: src/main/java/org/creekservice/api/test/conformity/ConformityCheck.java