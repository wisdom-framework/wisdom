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

import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.mail.Mail;
import org.ow2.chameleon.mail.MailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wisdom.api.configuration.ApplicationConfiguration;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * A implementation of the Mail Sender Service using SMTP.
 * This implementation can delegate to a <em>mock</em> version, just printing the message on the console but not
 * actually sending the mail.
 * Unlike the original chameleon service, this service does not use the Event Admin.
 */
@Component
@Provides
@Instantiate
public class Smtp implements MailSenderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Smtp.class.getName());

    public static final String MOCK_SERVER_NAME = "mock";
    public static final String DEFAULT_FROM = "mock-mailer@wisdom-framework.org";

    private static final String CONFHOST = "mail.smtp.host";
    private static final String CONFPORT = "mail.smtp.port";
    private static final String CONFAUTH = "mail.smtp.auth";
    private static final String SEPARATOR = "\t----";


    @Requires
    ApplicationConfiguration configuration;

    /**
     * Configuration properties.
     */
    private Properties properties;

    /**
     * Enables / Disabled debugging.
     */
    private boolean debug;

    /**
     * The mail address of the sender.
     */
    @ServiceProperty(name = MailSenderService.FROM_PROPERTY)
    protected String from;

    /**
     * The port.
     */
    protected int port;

    /**
     * The host.
     */
    protected String host;

    /**
     * The username.
     */
    protected String username;

    /**
     * The password.
     */
    protected String password;
    /**
     * The authenticator used for SSL.
     */
    private Authenticator sslAuthentication;

    /**
     * True we should use the mock server.
     */
    protected boolean useMock;
    protected Boolean useSmtps;
    protected Connection connection;

    /**
     * Configures the sender.
     */
    @Validate
    protected void configure() {
        host = configuration.getWithDefault(CONFHOST, MOCK_SERVER_NAME);
        from = configuration.getWithDefault("mail.smtp.from", DEFAULT_FROM);
        useMock = MOCK_SERVER_NAME.equals(host);

        properties = new Properties();
        useSmtps = configuration.getBooleanWithDefault("mail.smtps", false);
        if (!useSmtps) {
            port = configuration.getIntegerWithDefault(CONFPORT, 25);
        } else {
            port = configuration.getIntegerWithDefault(CONFPORT, 465);
        }

        properties.put(CONFHOST, host);
        properties.put(CONFPORT, port);
        properties.put("mail.smtps.quitwait", configuration.getBooleanWithDefault("mail.smtp.quitwait", false));

        connection = Connection.valueOf(configuration.getWithDefault("mail.smtp.connection",
                Connection.NO_AUTH.toString()));

        username = configuration.get("mail.smtp.username");
        password = configuration.get("mail.smtp.password");

        debug = configuration.getBooleanWithDefault("mail.smtp.debug", false);

        switch (connection) {
            case SSL:
                properties.put(CONFAUTH, Boolean.toString(true));
                properties.put("mail.smtp.socketFactory.port", Integer.toString(port));
                properties.put("mail.smtp.socketFactory.class", javax.net.ssl.SSLSocketFactory.class.getName());
                sslAuthentication = new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication() {
                        return new javax.mail.PasswordAuthentication(username, password);
                    }
                };
                break;
            case TLS:
                properties.put(CONFAUTH, Boolean.toString(true));
                properties.put("mail.smtp.starttls.enable", Boolean.toString(true));
                break;
            case NO_AUTH:
            default:
                properties.put(CONFAUTH, Boolean.toString(false));
                break;
        }

        LOGGER.info("Configuring Wisdom Mailer with:");
        @SuppressWarnings("unchecked") final Enumeration<String> enumeration =
                (Enumeration<String>) properties.propertyNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            LOGGER.info("\t" + name + ": " + properties.get(name));
        }
        if (username != null) {
            LOGGER.info("\tusername: " + username);
        }
        if (password != null) {
            LOGGER.info("\tpassword set but not displayed");
        }
        LOGGER.info("\tfrom: " + from);
    }

    @Updated
    public void reconfigure() {
        LOGGER.info("Reconfiguring the Wisdom Mailer");
        useMock = false;
        configure();
    }

    /**
     * Sends a mail.
     *
     * @param to      to
     * @param cc      cc
     * @param subject subject
     * @param body    body
     * @throws Exception if the mail cannot be sent.
     * @see org.ow2.chameleon.mail.MailSenderService#send(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void send(String to, String cc, String subject, String body)
            throws Exception {
        send(to, cc, subject, body, null);
    }

    /**
     * Sends a mail.
     *
     * @param to          to
     * @param cc          cc
     * @param subject     subject
     * @param body        body
     * @param attachments list of attachments
     * @throws Exception if the mail cannot be sent
     * @see org.ow2.chameleon.mail.MailSenderService#send(java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.util.List)
     */
    public void send(String to, String cc, String subject, String body,
                     List<File> attachments) throws Exception {
        if (attachments != null && !attachments.isEmpty()) {
            send(new Mail()
                    .to(to)
                    .cc(cc)
                    .subject(subject)
                    .body(body)
                    .attach(attachments));
        } else {
            send(new Mail()
                    .to(to)
                    .cc(cc)
                    .subject(subject)
                    .body(body));
        }
    }

    /**
     * Sends the given mail object.
     *
     * @param mail the mail
     * @throws Exception if the mail cannot be sent.
     */
    public void send(Mail mail) throws Exception {
        if (mail.to() == null || mail.to().isEmpty()) {
            throw new IllegalArgumentException("The given 'to' is null or empty");
        }

        if (mail.from() == null) {
            mail.from(from);
        }

        if (useMock) {
            sendMessageWithMockServer(mail);
            return;
        }

        Transport transport = null;
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(Smtp.class.getClassLoader());
            Session session = Session.getDefaultInstance(properties, sslAuthentication);
            //Session.getInstance(properties, sslAuthentication);

            session.setDebug(debug);
            // create a message
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(from));

            // Manage to.
            List<String> to = mail.to();
            InternetAddress[] address = new InternetAddress[to.size()];
            for (int index = 0; index < to.size(); index++) {
                String t = to.get(index);
                if (t == null) {
                    throw new NullPointerException("A 'to' address is null");
                } else {
                    address[index] = new InternetAddress(t);
                }
            }
            msg.setRecipients(Message.RecipientType.TO, address);

            // Manage cc.
            List<String> cc = mail.cc();
            InternetAddress[] addressCC = new InternetAddress[cc.size()];
            for (int index = 0; index < cc.size(); index++) {
                String t = cc.get(index);
                if (t == null) {
                    throw new NullPointerException("A 'cc' address is null");
                } else {
                    addressCC[index] = new InternetAddress(t);
                }
            }
            msg.setRecipients(Message.RecipientType.CC, addressCC);


            msg.setSubject(mail.subject());

            Date sent = new Date();
            msg.setSentDate(sent);
            mail.sent(sent);

            // create the Multipart and its parts to it
            Multipart mp = new MimeMultipart();

            // create and fill the first message part
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(mail.body());
            mp.addBodyPart(mbp1);

            List<File> attachments = mail.attachments();
            if (attachments != null && !attachments.isEmpty()) {
                for (File file : attachments) {
                    MimeBodyPart part = new MimeBodyPart();
                    DataSource source = new FileDataSource(file);
                    part.setDataHandler(new DataHandler(source));
                    part.setFileName(file.getName());
                    mp.addBodyPart(part);
                }
            }

            // add the Multipart to the message
            msg.setContent(mp);

            // send the message
            if (useSmtps) {
                transport = session.getTransport("smtps");
            } else {
                transport = session.getTransport("smtp");
            }
            if (connection == Connection.TLS) {
                transport.connect(host,
                        port, username, password);
            } else {
                transport.connect();
            }
            transport.sendMessage(msg, msg.getAllRecipients());
        } finally {
            Thread.currentThread().setContextClassLoader(original);
            if (transport != null) {
                transport.close();
            }
        }
    }

    private void sendMessageWithMockServer(Mail mail) {
        LOGGER.info("Sending mail:");
        LOGGER.info("\tFrom: " + mail.from());
        LOGGER.info("\tTo: " + mail.to());
        if (!mail.cc().isEmpty()) {
            LOGGER.info("\tCC: " + mail.cc());
        }
        LOGGER.info("\tSubject: " + mail.subject());
        LOGGER.info(SEPARATOR);
        LOGGER.info(mail.body());
        LOGGER.info(SEPARATOR);
    }

    /**
     * Type of connections.
     */
    public enum Connection {
        NO_AUTH,
        TLS,
        SSL
    }

}
