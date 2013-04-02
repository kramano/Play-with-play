package controllers;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import play.api.mvc.AnyContent;
import play.api.mvc.AnyContentAsXml;
import play.db.DB;
import play.libs.Scala;
import play.mvc.Result;
import play.test.FakeRequest;
import play.test.Helpers;
import play.test.WithApplication;
import scala.collection.Seq;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

/**
 * Author: Maxim Novik
 */
public class ServiceTest extends WithApplication {

    final private static String CREATE_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>    \n" +
            "<request>\n" +
            "        <request-type>CREATE-AGT</request-type>\n" +
            "        <extra name = \"login\">123456</extra>\n" +
            "        <extra name = \"password\">pwd</extra>\n" +
            "</request>";

    final private static String UNKNOWN_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>    \n" +
            "<request>\n" +
            "        <request-type>UNKNOWN</request-type>\n" +
            "        <extra name = \"login\">123456</extra>\n" +
            "        <extra name = \"password\">pwd</extra>\n" +
            "</request>";

    private static final String MISSING_PARAM_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>    \n" +
            "<request>\n" +
            "        <extra name = \"login\">123456</extra>\n" +
            "        <extra name = \"password\">pwd</extra>\n" +
            "</request>";

    private static final String BALANCE_REQUEST = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>    \n" +
            "<request>\n" +
            "        <request-type>GET-BALANCE</request-type>\n" +
            "        <extra name = \"login\">123456</extra>\n" +
            "        <extra name = \"password\">pwd</extra>\n" +
            "</request>";

    @Before
    public void setUp() {
        start(fakeApplication(inMemoryDatabase()));
        Connection connection = DB.getConnection();
        PreparedStatement createClients = null;
        try {
            createClients =
                    connection.prepareStatement("CREATE TABLE CLIENTS (\n" +
                            "ID BIGINT PRIMARY KEY AUTO_INCREMENT,\n" +
                            "LOGIN VARCHAR(255) UNIQUE,\n" +
                            "PASSWORD VARCHAR(255) NOT NULL,\n" +
                            "BALANCE DECIMAL(19, 4) DEFAULT 0 \n" +
                            ");");
            createClients.executeUpdate();
            createClients.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldCreateNewClientSuccess() {
        Result response = callAction(controllers.routes.ref.Application.process(), fakeRequest(CREATE_REQUEST));
        assertThat(status(response)).isEqualTo(OK);
        assertThat(contentType(response)).isEqualTo("text/xml");
        assertThat(charset(response)).isEqualTo("utf-8");
        assertThat(contentAsString(response)).contains("<result-code>0</result-code>");
    }

    @Test
    public void shouldNotCreateNewClientWithExistingLogin() {
        callAction(controllers.routes.ref.Application.process(), fakeRequest(CREATE_REQUEST));
        Result response = callAction(controllers.routes.ref.Application.process(), fakeRequest(CREATE_REQUEST));
        assertThat(status(response)).isEqualTo(OK);
        assertThat(contentType(response)).isEqualTo("text/xml");
        assertThat(charset(response)).isEqualTo("utf-8");
        assertThat(contentAsString(response)).contains("<result-code>1</result-code>");
    }

    @Test
    public void shouldReturnErrorForNonXmlRequest() {
        Result response = callAction(controllers.routes.ref.Application.process(), new FakeRequest("POST", "/"));
        assertThat(status(response)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(response)).contains("Expecting xml data");
    }

    @Test
    public void shouldReturnErrorForUnknownType() {
        Result response = callAction(controllers.routes.ref.Application.process(), fakeRequest(UNKNOWN_REQUEST));
        assertThat(status(response)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(response)).contains("Unknown request type");
    }

    @Test
    public void shouldReturnErrorForMissingParameter() {
        Result response = callAction(controllers.routes.ref.Application.process(), fakeRequest(MISSING_PARAM_REQUEST));
        assertThat(status(response)).isEqualTo(BAD_REQUEST);
        assertThat(contentAsString(response)).contains("Missing parameter");
    }

    @Test
    public void shouldReturnBalanceForExistingClient() {
        //create client
        callAction(controllers.routes.ref.Application.process(), fakeRequest(CREATE_REQUEST));

        Result response = callAction(controllers.routes.ref.Application.process(), fakeRequest(BALANCE_REQUEST));
        assertThat(status(response)).isEqualTo(OK);
        assertThat(contentType(response)).isEqualTo("text/xml");
        assertThat(charset(response)).isEqualTo("utf-8");
        assertThat(contentAsString(response)).contains("<result-code>0</result-code>");
        assertThat(contentAsString(response)).contains("<extra name=\"balance\">0.0000</extra>");
    }

    @Test
    public void shouldReturnErrorForNoClient() {
        Result response = callAction(controllers.routes.ref.Application.process(), fakeRequest(BALANCE_REQUEST));
        assertThat(status(response)).isEqualTo(OK);
        assertThat(contentType(response)).isEqualTo("text/xml");
        assertThat(charset(response)).isEqualTo("utf-8");
        assertThat(contentAsString(response)).contains("<result-code>3</result-code>");
    }

    @Test
    public void shouldReturnErrorForWrongPassword() {
        //create client
        callAction(controllers.routes.ref.Application.process(), fakeRequest(CREATE_REQUEST));
        // set wrong password
        String wrongPasswordRequest = BALANCE_REQUEST.replace("pwd", "wrong");
        Result response = callAction(controllers.routes.ref.Application.process(), fakeRequest(wrongPasswordRequest));
        assertThat(status(response)).isEqualTo(OK);
        assertThat(contentType(response)).isEqualTo("text/xml");
        assertThat(charset(response)).isEqualTo("utf-8");
        assertThat(contentAsString(response)).contains("<result-code>4</result-code>");
    }

    // Dirty hacks to create fake request with xml body.
    private FakeRequest fakeRequest(String xml) {
        return withXmlBody(new InputSource(new StringReader(xml)));
    }

    private play.test.FakeRequest withXmlBody(InputSource xml) {
        return withAnyContent(new AnyContentAsXml(scala.xml.XML.load(xml)), "application/xml", Helpers.POST);
    }

    private play.test.FakeRequest withAnyContent(AnyContent content, String contentType, String method) {
        play.api.test.FakeRequest fake = play.api.test.FakeRequest.apply();
        Map<String, Seq<String>> map = new HashMap<String, Seq<String>>(Scala.asJava(fake.headers().toMap()));
        map.put("Content-Type", Scala.toSeq(new String[] {contentType}));
        fake = new play.api.test.FakeRequest(method, fake.uri(),
                new play.api.test.FakeHeaders(Scala.asScala(map).toSeq()),
                content, fake.remoteAddress(), fake.version(), fake.id(), fake.tags());
        play.test.FakeRequest fr = new play.test.FakeRequest();
        try {
            Field field = fr.getClass().getDeclaredField("fake");
            field.setAccessible(true);
            field.set(fr, fake);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fr;
    }
}
