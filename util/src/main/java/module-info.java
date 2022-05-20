module creek.test.util {
    requires transitive creek.base.annotation;
    requires transitive com.github.spotbugs.annotations;
    requires transitive java.management;

    exports org.creekservice.api.test.util;
    exports org.creekservice.api.test.util.debug;
    exports org.creekservice.api.test.util.coverage;
}
