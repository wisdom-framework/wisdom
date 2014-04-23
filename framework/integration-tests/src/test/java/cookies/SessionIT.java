package cookies;

import org.apache.http.cookie.Cookie;
import org.junit.Test;
import org.wisdom.test.http.HttpResponse;
import org.wisdom.test.parents.WisdomBlackBoxTest;

import static org.assertj.core.api.Assertions.assertThat;

public class SessionIT extends WisdomBlackBoxTest {

    @Test
    public void testSessionManipulation() throws Exception {
        HttpResponse<String> response = get("/session").asString();
        Cookie session = response.cookie("wisdom_SESSION");
        assertThat(session).isNotNull();
        assertThat(session.getPath()).isEqualTo("/");
        assertThat(session.getValue()).contains("foo=bar").contains("baz=bah").contains("blah=42");

        response = get("/session").asString();
        session = response.cookie("wisdom_SESSION");
        // blah removed
        assertThat(session.getValue()).contains("foo=bar").contains("baz=bah").doesNotContain("blah");

        response = get("/session/clear").asString();
        session = response.cookie("wisdom_SESSION");
        // Session cleared... no more cookie.
        assertThat(session).isNull();
    }

    @Test
    public void testSessionWithAnotherCookie() throws Exception {
        HttpResponse<String> response = get("/session/cookie").asString();
        Cookie session = response.cookie("wisdom_SESSION");
        Cookie cookie = response.cookie("toto");

        assertThat(session).isNotNull();
        assertThat(cookie).isNotNull();
        assertThat(session.getPath()).isEqualTo("/");
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(session.getValue()).contains("foo=bar");
        assertThat(cookie.getValue()).contains("titi");

        // Clear the cookie
        response = get("/session/cookie/clear").asString();
        session = response.cookie("wisdom_SESSION");
        cookie = response.cookie("toto");
        assertThat(session).isNotNull();
        assertThat(cookie).isNull();

        response = get("/session/clear").asString();
        session = response.cookie("wisdom_SESSION");
        assertThat(session).isNull();
    }
}
