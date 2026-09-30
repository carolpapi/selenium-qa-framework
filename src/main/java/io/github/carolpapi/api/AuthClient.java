package io.github.carolpapi.api;

import io.github.carolpapi.core.config.Config;

import java.util.Map;

public class AuthClient extends ApiClient {

    private static volatile String cachedToken;

    /** Token is requested once and reused: fewer calls, faster and more stable suites. */
    public String token() {
        if (cachedToken == null) {
            synchronized (AuthClient.class) {
                if (cachedToken == null) {
                    cachedToken = request()
                            .body(Map.of(
                                    "username", Config.get("api.username"),
                                    "password", Config.get("api.password")))
                            .post("/auth")
                            .then().statusCode(200)
                            .extract().path("token");
                }
            }
        }
        return cachedToken;
    }
}
