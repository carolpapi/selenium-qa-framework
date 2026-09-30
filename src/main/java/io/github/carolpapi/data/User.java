package io.github.carolpapi.data;

import io.github.carolpapi.core.config.Config;

/**
 * Test users of the application under test. Each one represents a business
 * scenario (normal customer, blocked account, slow backend...), so tests read
 * like requirements: loginAs(User.LOCKED_OUT).
 */
public enum User {

    STANDARD("standard_user"),
    LOCKED_OUT("locked_out_user"),
    PROBLEM("problem_user"),
    /** Backend answers slowly on login: the perfect case to prove we wait for conditions, not time. */
    PERFORMANCE_GLITCH("performance_glitch_user");

    private final String username;

    User(String username) {
        this.username = username;
    }

    public String username() {
        return username;
    }

    /** Password comes from configuration (overridable by CI secrets), never hard-coded in tests. */
    public String password() {
        return Config.get("sauce.password");
    }
}
