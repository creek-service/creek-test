module creek.test.conformity {
    requires transitive creek.base.annotation;
    requires io.github.classgraph;

    exports org.creekservice.api.test.conformity;
    exports org.creekservice.api.test.conformity.check;
}
