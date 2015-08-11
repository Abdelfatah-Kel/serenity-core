package net.serenitybdd.rest.stubs;

import com.jayway.restassured.internal.mapper.ObjectMapperType;
import com.jayway.restassured.mapper.ObjectMapper;
import com.jayway.restassured.path.json.JsonPath;
import com.jayway.restassured.path.json.config.JsonPathConfig;
import com.jayway.restassured.path.xml.XmlPath;
import com.jayway.restassured.path.xml.config.XmlPathConfig;
import com.jayway.restassured.response.*;
import org.mockito.Mockito;

import java.io.InputStream;
import java.util.Map;

/**
 * Created by john on 23/07/2015.
 */
public class ResponseStub implements Response {
    @Override
    public ValidatableResponse then() {
        return new ValidatableResponseStub();
    }

    @Override
    public String print() {
        return null;
    }

    @Override
    public String prettyPrint() {
        return null;
    }

    @Override
    public Response peek() {
        return new ResponseStub();
    }

    @Override
    public Response prettyPeek() {
        return new ResponseStub();
    }

    @Override
    public <T> T as(Class<T> cls) {
        return null;
    }

    @Override
    public <T> T as(Class<T> cls, ObjectMapperType mapperType) {
        return null;
    }

    @Override
    public <T> T as(Class<T> cls, ObjectMapper mapper) {
        return null;
    }

    @Override
    public JsonPath jsonPath() {
        return new JsonPath("");
    }

    @Override
    public JsonPath jsonPath(JsonPathConfig config) {
        return new JsonPath("");
    }

    @Override
    public XmlPath xmlPath() {
        return new XmlPath("");
    }

    @Override
    public XmlPath xmlPath(XmlPathConfig config) {
        return new XmlPath("");
    }

    @Override
    public XmlPath xmlPath(XmlPath.CompatibilityMode compatibilityMode) {
        return new XmlPath("");
    }

    @Override
    public XmlPath htmlPath() {
        return new XmlPath("");
    }

    @Override
    public <T> T path(String path, String... arguments) {
        return null;
    }

    @Override
    public String asString() {
        return null;
    }

    @Override
    public byte[] asByteArray() {
        return new byte[0];
    }

    @Override
    public InputStream asInputStream() {
        return null;
    }

    @Override
    public Response andReturn() {
        return this;
    }

    @Override
    public Response thenReturn() {
        return this;
    }

    @Override
    public ResponseBody body() {
         return Mockito.mock(ResponseBody.class);
    }

    @Override
    public ResponseBody getBody() {
        return null;
    }

    @Override
    public Headers headers() {
        return null;
    }

    @Override
    public Headers getHeaders() {
        return null;
    }

    @Override
    public String header(String name) {
        return null;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Map<String, String> cookies() {
        return null;
    }

    @Override
    public Cookies detailedCookies() {
        return null;
    }

    @Override
    public Map<String, String> getCookies() {
        return null;
    }

    @Override
    public Cookies getDetailedCookies() {
        return null;
    }

    @Override
    public String cookie(String name) {
        return null;
    }

    @Override
    public String getCookie(String name) {
        return null;
    }

    @Override
    public Cookie detailedCookie(String name) {
        return null;
    }

    @Override
    public Cookie getDetailedCookie(String name) {
        return null;
    }

    @Override
    public String contentType() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String statusLine() {
        return null;
    }

    @Override
    public String getStatusLine() {
        return null;
    }

    @Override
    public String sessionId() {
        return null;
    }

    @Override
    public String getSessionId() {
        return null;
    }

    @Override
    public int statusCode() {
        return 0;
    }

    @Override
    public int getStatusCode() {
        return 0;
    }
}
