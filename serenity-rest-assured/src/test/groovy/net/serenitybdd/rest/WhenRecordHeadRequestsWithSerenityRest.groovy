package net.serenitybdd.rest

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import net.serenity.test.utils.rules.TestCase
import serenitymodel.net.serenitybdd.core.rest.RestQuery
import serenitycore.net.thucydides.core.steps.BaseStepListener
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import static serenitymodel.net.serenitybdd.core.rest.RestMethod.HEAD
import static net.serenitybdd.rest.SerenityRest.*
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse
import static com.github.tomakehurst.wiremock.client.WireMock.matching
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor
import static net.serenitybdd.rest.DecomposedContentType.*;


/**
 * User: YamStranger
 * Date: 11/29/15
 * Time: 5:58 PM
 */
class WhenRecordHeadRequestsWithSerenityRest extends Specification {

    @Rule
    def WireMockRule wire = new WireMockRule(0);

    @Rule
    def TestCase<BaseStepListener> test = new TestCase({
        Mock(BaseStepListener);
    }.call());

    @Rule
    TemporaryFolder temporaryFolder

    def Gson gson = new GsonBuilder().setPrettyPrinting().
        serializeNulls().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    def "Should record RestAssured head() method calls"() {
        given:
            def JsonObject json = new JsonObject()
            json.addProperty("Number", "9999")
            json.addProperty("Price", "100")
            def body = gson.toJson(json)

            def base = "http://localhost:${wire.port()}"
            def path = "/test/number"
            def url = "$base$path"

            stubFor(WireMock.head(urlEqualTo(path))
                .withRequestBody(matching(".*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON.asString())
                .withBody(body)));
        when:
            def result = head(url).then()
        then: "The JSON request should be recorded in the test steps"
            1 * test.firstListener().recordRestQuery(*_) >> { RestQuery query ->
                assert "$query" == "HEAD $url"
                assert query.method == HEAD
                assert "${query.path}" == url
                assert query.statusCode == 200
            }
        and:
            result.statusCode(200)
    }

    def "Should record RestAssured head() method calls with parameters"() {
        given:
            def JsonObject json = new JsonObject()
            json.addProperty("Exists", true)
            json.addProperty("label", "UI")
            def body = gson.toJson(json)

            def base = "http://localhost:${wire.port()}"
            def path = "/test/label"
            def url = "$base$path"

            stubFor(WireMock.head(urlPathMatching("$path.*"))
                .withRequestBody(matching(".*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON.asString())
                .withBody(body)));
        when:
            def result = head("$url?status={status}", ["status": "available"]).then()
        then: "The JSON request should be recorded in the test steps"
            1 * test.firstListener().recordRestQuery(*_) >> { RestQuery query ->
                assert "$query" == "HEAD $url?status=available"
                assert query.method == HEAD
                assert query.statusCode == 200
            }
        and:
            result.statusCode(200)
    }

    def "Should record RestAssured head() method calls with parameter provided as a list"() {
        given:
            def JsonObject json = new JsonObject()
            json.addProperty("Weather", "rain")
            json.addProperty("temperature", "+2")
            def body = gson.toJson(json)

            def base = "http://localhost:${wire.port()}"
            def path = "/test/weather"
            def url = "$base$path"

            stubFor(WireMock.head(urlPathMatching("$path.*"))
                .withRequestBody(matching(".*"))
                .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", APPLICATION_JSON.asString())
                .withBody(body)));
        when:
            def result = head("$url?status={status}", "available").then()
        then: "The JSON request should be recorded in the test steps"
            1 * test.firstListener().recordRestQuery(*_) >> { RestQuery query ->
                assert "$query" == "HEAD $url?status=available"
                assert query.method == HEAD
                assert query.statusCode == 200
            }
        and:
            result.statusCode(200)
    }
}
