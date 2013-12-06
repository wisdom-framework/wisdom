package org.wisdom.mailer;

import javax.inject.Inject;

import org.apache.felix.ipojo.Pojo;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.ow2.chameleon.mail.Mail;
import org.ow2.chameleon.mail.MailSenderService;
import org.wisdom.test.parents.WisdomTest;

import java.util.Properties;

/**
 * Check the Smtp service.
 */
public class SmtpIT extends WisdomTest {
    public static final String USERNAME = "ow2.chameleon.test@googlemail.com";
    public static final String PASSWORD = "chameleon";

    @Inject
    MailSenderService mailer;

    @Test
    @Category(Mock.class)
    public void mock() throws Exception {
        mailer.send(new Mail()
            .to("clement@wisdom.org")
            .subject("Hello from wisdom")
            .body("Hi !"));
    }

    @Test
    @Category(Real.class)
    public void gmail() throws Exception {
        Properties props = getGmailProperties();
        ((Pojo) mailer).getComponentInstance().reconfigure(props);

        mailer.send(new Mail()
                .from(USERNAME)
                .to("clement.escoffier@gmail.com")
                .subject("[IT-TEST] Hello from wisdom")
                .body("This is a test. Wisdom is sending this mail using its own mailer service. \n Wisdom"));
    }

    private Properties getGmailProperties() {
        Properties properties = new Properties();
        properties.setProperty("smtp.connection", "SSL");
        properties.setProperty("smtp.host", "smtp.gmail.com");
        properties.setProperty("smtp.port", "465");
        properties.setProperty("smtp.from", USERNAME);
        properties.setProperty("smtp.username", USERNAME);
        properties.setProperty("smtp.password", PASSWORD);
        properties.setProperty("smtp.debug", "true");
        return properties;
    }
}
