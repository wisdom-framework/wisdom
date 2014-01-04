package org.wisdom.mailer;

import javax.inject.Inject;

import org.apache.felix.ipojo.Pojo;
import org.junit.After;
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

    @After
    public void tearDown() {
        // Clear all related system properties
        System.clearProperty("mail.smtp.connection");
        System.clearProperty("mail.smtp.host");
        System.clearProperty("mail.smtp.port");
        System.clearProperty("mail.smtp.from");
        System.clearProperty("mail.smtp.username");
        System.clearProperty("mail.smtp.password");
        System.clearProperty("mail.smtp.debug");
    }

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
        // We set the system properties to use gmail and force the instance to be reconfigured.
        setGmailProperties();
        ((Pojo) mailer).getComponentInstance().reconfigure(new Properties());

        mailer.send(new Mail()
                .from(USERNAME)
                .to("clement.escoffier@gmail.com")
                .subject("[IT-TEST] Hello from wisdom")
                .body("This is a test. Wisdom is sending this mail using its own mailer service. \n Wisdom"));
    }

    private void setGmailProperties() {
        System.setProperty("mail.smtp.connection", "SSL");
        System.setProperty("mail.smtp.host", "smtp.gmail.com");
        System.setProperty("mail.smtp.port", "465");
        System.setProperty("mail.smtp.from", USERNAME);
        System.setProperty("mail.smtp.username", USERNAME);
        System.setProperty("mail.smtp.password", PASSWORD);
        System.setProperty("mail.smtp.debug", "true");
    }
}
