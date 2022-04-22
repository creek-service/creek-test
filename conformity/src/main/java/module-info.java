module creek.test.conformity {
    requires transitive creek.base.annotation;
    requires io.github.classgraph;

    exports org.creek.api.test.conformity;
    exports org.creek.api.test.conformity.check;
}
