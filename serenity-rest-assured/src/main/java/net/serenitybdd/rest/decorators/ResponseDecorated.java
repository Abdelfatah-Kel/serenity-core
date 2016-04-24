package net.serenitybdd.rest.decorators;

import com.jayway.restassured.internal.RestAssuredResponseOptionsImpl;
import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.mapper.ObjectMapper;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.json.config.JsonPathConfig;
import com.jayway.restassured.path.xml.XmlPath;
import com.jayway.restassured.path.xml.config.XmlPathConfig;
import com.jayway.restassured.response.*;

import java.io.InputStream;
import java.util.Map;

/**
 * User: YamStranger
 * Date: 3/22/16
 * Time: 10:30 PM
 */
public class ResponseDecorated extends RestAssuredResponseOptionsImpl<Response> implements Response {
    private final Response core;

    public ResponseDecorated(final Response core) {
        this.core = core;
    }

    @Override
    public ValidatableResponse then() {
        return core.then();
    }

    @Override
    public String print() {
        return core.print();
    }

    @Override
    public String prettyPrint() {
        return core.prettyPrint();
    }

    @Override
    public Response peek() {
        return core.peek();
    }

    @Override
    public Response prettyPeek() {
        return core.prettyPeek();
    }

    @Override
    public <T> T as(final Class<T> cls) {
        return core.as(cls);
    }

    @Override
    public <T> T as(final Class<T> cls, final ObjectMapperType mapperType) {
        return core.as(cls, mapperType);
    }

    @Override
    public <T> T as(final Class<T> cls, final ObjectMapper mapper) {
        return core.as(cls, mapper);
    }

    @Override
    public JsonPath jsonPath() {
        return core.jsonPath();
    }

    @Override
    public JsonPath jsonPath(final JsonPathConfig config) {
        return core.jsonPath(config);
    }

    @Override
    public XmlPath xmlPath() {
        return core.xmlPath();
    }

    @Override
    public XmlPath xmlPath(final XmlPathConfig config) {
        return core.xmlPath(config);
    }

    @Override
    public XmlPath xmlPath(final XmlPath.CompatibilityMode compatibilityMode) {
        return core.xmlPath(compatibilityMode);
    }

    @Override
    public XmlPath htmlPath() {
        return core.htmlPath();
    }

    @Override
    public <T> T path(final String path, final String... arguments) {
        return core.path(path, arguments);
    }

    @Override
    public String asString() {
        return core.asString();
    }

    @Override
    public byte[] asByteArray() {
        return core.asByteArray();
    }

    @Override
    public InputStream asInputStream() {
        return core.asInputStream();
    }

    @Override
    public Response andReturn() {
        return core.andReturn();
    }

    @Override
    public Response thenReturn() {
        return core.thenReturn();
    }

    @Override
    public ResponseBody body() {
        return core.body();
    }

    @Override
    public ResponseBody getBody() {
        return core.getBody();
    }

    @Override
    public Headers headers() {
        return core.headers();
    }

    @Override
    public Headers getHeaders() {
        return core.getHeaders();
    }

    @Override
    public String header(final String name) {
        return core.header(name);
    }

    @Override
    public String getHeader(final String name) {
        return core.getHeader(name);
    }

    @Override
    public Map<String, String> cookies() {
        return core.cookies();
    }

    @Override
    public Cookies detailedCookies() {
        return core.detailedCookies();
    }

    @Override
    public Map<String, String> getCookies() {
        return core.getCookies();
    }

    @Override
    public Cookies getDetailedCookies() {
        return core.getDetailedCookies();
    }

    @Override
    public String cookie(final String name) {
        return core.cookie(name);
    }

    @Override
    public String getCookie(final String name) {
        return core.getCookie(name);
    }

    @Override
    public Cookie detailedCookie(final String name) {
        return core.detailedCookie(name);
    }

    @Override
    public Cookie getDetailedCookie(final String name) {
        return core.getDetailedCookie(name);
    }

    @Override
    public String contentType() {
        return core.contentType();
    }

    @Override
    public String getContentType() {
        return core.getContentType();
    }

    @Override
    public String statusLine() {
        return core.statusLine();
    }

    @Override
    public String getStatusLine() {
        return core.getStatusLine();
    }

    @Override
    public String sessionId() {
        return core.sessionId();
    }

    @Override
    public String getSessionId() {
        return core.getSessionId();
    }

    @Override
    public int statusCode() {
        return core.statusCode();
    }

    @Override
    public int getStatusCode() {
        return core.getStatusCode();
    }
}