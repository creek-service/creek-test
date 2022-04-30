module creek.test.util {
    requires transitive creek.base.annotation;
    requires transitive com.github.spotbugs.annotations;
    requires transitive java.management;

    exports org.creek.api.test.util;
    exports org.creek.api.test.util.coverage;
}
