package org.wisdom.mailer;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ow2.chameleon.mail.Mail;
import org.wisdom.api.configuration.ApplicationConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Check the mail service.
 * Only the mock server is tested.
 */
public class SmtpTest {

    @Test
    public void testCreationAndDefaults() throws Exception {
        Smtp smtp = new Smtp();
        smtp.configuration = mock(ApplicationConfiguration.class);

        when(smtp.configuration.getWithDefault(anyString(), anyString())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[1];
            }
        });

        when(smtp.configuration.getIntegerWithDefault(anyString(), anyInt())).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return invocation.getArguments()[1];
            }
        });

        smtp.configure();

        smtp.send(new Mail().to("me@me.com").subject("subject").body("Hello"));

        assertThat(smtp.from).contains("mock-mailer");
        assertThat(smtp.port).isEqualTo(25);
        assertThat(smtp.host).isEqualTo("mock");
        assertThat(smtp.username).isNull();
        assertThat(smtp.password).isNull();
    }

}
