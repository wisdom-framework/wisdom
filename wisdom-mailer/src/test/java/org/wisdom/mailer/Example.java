/*
 * #%L
 * Wisdom-Framework
 * %%
 * Copyright (C) 2013 - 2014 Wisdom Framework
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
