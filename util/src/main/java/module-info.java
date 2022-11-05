/**
 * Module containing common code to help with testing
 *
 * <p>Intended for internal Creek use only
 */
module creek.test.util {
    requires transitive com.github.spotbugs.annotations;
    requires transitive java.management;

    exports org.creekservice.api.test.util;
    exports org.creekservice.api.test.util.debug;
    exports org.creekservice.api.test.util.coverage;
}
