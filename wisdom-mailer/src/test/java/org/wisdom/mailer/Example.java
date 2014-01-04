package org.wisdom.mailer;

import org.apache.felix.ipojo.annotations.Requires;
import org.ow2.chameleon.mail.Mail;
import org.ow2.chameleon.mail.MailSenderService;
import org.wisdom.api.DefaultController;
import org.wisdom.api.annotations.Controller;
import java.io.File;

@Controller
public class Example extends DefaultController {

    @Requires
    MailSenderService mailer;

    public void sendMail() throws Exception {
        // Send a simple mail
        mailer.send(new Mail()
                .to("me@wisdom-framework.org")
                .subject("Welcome to Wisdom")
                .body("Hello !"));

        // Send a mail with an attachment
        mailer.send(new Mail()
                .to("me@wisdom-framework.org")
                .subject("Wisdom Log")
                .body("Here is the current Wisdom Log file")
                .attach(new File("logs/wisdom.log")));
    }
}
