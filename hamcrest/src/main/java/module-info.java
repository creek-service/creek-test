/**
 * Creek Hamcrest matchers
 *
 * <p>Intended for internal Creek use only
 */
module creek.test.hamcrest {
    requires transitive org.hamcrest;
    requires transitive com.github.spotbugs.annotations;

    exports org.creekservice.api.test.hamcrest;
}
