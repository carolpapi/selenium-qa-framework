package io.github.carolpapi.api;

import io.github.carolpapi.core.config.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.ErrorLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import static io.restassured.RestAssured.given;

/**
 * Base for API clients: base URI, content type and logging are defined once.
 * Clients expose business operations (createBooking), tests only assert.
 * Same idea as the Page Object Model, applied to APIs.
 */
public abstract class ApiClient {

    protected RequestSpecification request() {
        return given().spec(new RequestSpecBuilder()
                .setBaseUri(Config.get("api.base.url"))
                .setContentType(ContentType.JSON)
                // Exact value on purpose: ContentType.JSON as Accept expands to
                // "application/json, application/javascript, text/javascript, text/json"
                // and this API answers 418 (I'm a teapot) to anything but "application/json".
                .setAccept("application/json")
                // Logs request/response only when the status is 4xx/5xx: clean logs,
                // but full evidence when something breaks in the pipeline.
                .addFilter(new ErrorLoggingFilter())
                .build());
    }
}
