/**
 * Module containing types for enforcing certain coding rules in Creek code.
 *
 * <p>Thereby removing the need to pick such things up in code reviews.
 *
 * <p>Intended for internal Creek use only
 */
module creek.test.conformity {
    requires io.github.classgraph;

    exports org.creekservice.api.test.conformity;
    exports org.creekservice.api.test.conformity.check;
}
