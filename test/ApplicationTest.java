import org.junit.Test;
import play.mvc.Content;
import views.xml.getBalance;

import java.math.BigDecimal;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    @Test
    public void shouldRenderNewClientResponse() {
        Content response = views.xml.newClientResponse.render(0);
        checkResponse(response);
        assertThat(contentAsString(response)).contains("<result-code>0</result-code>");
    }

    @Test
    public void shouldRenderValidGetBalanceResponse() {
        Content response = getBalance.render(0, new BigDecimal("10.00"));
        checkResponse(response);
        assertThat(contentAsString(response)).contains("<result-code>0</result-code>");
        assertThat(contentAsString(response)).contains("<extra name=\"balance\">10.00</extra>");
    }

    @Test
    public void shouldRenderInvalidGetBalanceResponse() {
        Content response = getBalance.render(3, null);
        checkResponse(response);
        assertThat(contentAsString(response)).contains("<result-code>3</result-code>");
        assertThat(contentAsString(response)).doesNotContain("extra name");
    }

    private void checkResponse(Content response) {
        assertThat(contentType(response)).isEqualTo("text/xml");
        assertThat(contentAsString(response)).contains("<response>");
    }



}
