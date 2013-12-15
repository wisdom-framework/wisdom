package org.wisdom.mailer;

import com.sun.mail.smtp.SMTPSSLTransport;
import com.sun.mail.smtp.SMTPTransport;
import org.apache.felix.ipojo.annotations.*;
import org.ow2.chameleon.mail.Mail;
import org.ow2.chameleon.mail.MailSenderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    /**
     * Configuration properties.
     */
    private Properties m_properties;
    /**
     * Enables / Disabled debugging.
     */
    @Property(name = "smtp.debug", value = "false")
    private boolean m_debug;
    /**
     * The mail address of the sender.
     */
    @Property(name = "smtp.from", mandatory = true, value = "mock-mailer@wisdom-framework.io")
    @ServiceProperty(name = MailSenderService.FROM_PROPERTY)
    private String m_from;
    /**
     * The port.
     */
    @Property(name = "smtp.port", mandatory = true, value="465")
    private int m_port;
    /**
     * The host.
     */
    @Property(name = "smtp.host", mandatory = true, value = MOCK_SERVER_NAME)
    private String m_host;
    /**
     * Does quit should wait until termination.
     */
    @Property(name = "smtp.quitwait", value = "false")
    private boolean m_quitWait;
    /**
     * Enables /Disables SMTPS.
     */
    @Property(name = "smtp.useSMTPS", value = "false")
    private boolean m_useSMTPS;
    /**
     * The username.
     */
    @Property(name = "smtp.username")
    private String m_username;
    /**
     * The password.
     */
    @Property(name = "smtp.password")
    private String m_password;
    /**
     * The authenticator used for SSL.
     */
    private Authenticator sslAuthentication;
    /**
     * The connection type.
     */
    @Property(name = "smtp.connection", mandatory = true, value = "NO_AUTH")
    private Connection m_connection;

    /**
     * True we should use the mock server.
     */
    private boolean m_mock;

    public Smtp() {
        configure();
    }

    /**
     * Configures the sender.
     */
    private void configure() {
        m_properties = new Properties();
        if (MOCK_SERVER_NAME.equals(m_host)) {
            m_mock = true;
            return;
        }
        m_mock = false;
        m_properties.put("mail.smtp.host", m_host);
        m_properties.put("mail.smtp.port", Integer.toString(m_port));

        m_properties.put("mail.smtps.quitwait", m_quitWait);
        switch (m_connection) {
            case SSL:
                m_properties.put("mail.smtp.auth", Boolean.toString(true));
                m_properties.put("mail.smtp.socketFactory.port", Integer.toString(m_port));
                m_properties.put("mail.smtp.socketFactory.class", javax.net.ssl.SSLSocketFactory.class.getName());
                sslAuthentication = new javax.mail.Authenticator() {
                    protected javax.mail.PasswordAuthentication getPasswordAuthentication(){
                        return new javax.mail.PasswordAuthentication(m_username, m_password);
                    }
                };
                break;
            case TLS:
                m_properties.put("mail.smtp.auth", Boolean.toString(true));
                m_properties.put("mail.smtp.starttls.enable", Boolean.toString(true));
                break;
            case NO_AUTH:
                m_properties.put("mail.smtp.auth", Boolean.toString(false));
                break;
        }
    }

    @Updated
    public void reconfigure() {
        m_mock = false;
        configure();
    }

    /**
     * Sends a mail.
     * @param to to
     * @param cc cc
     * @param subject subject
     * @param body body
     * @throws Exception if the mail cannot be sent.
     * @see org.ow2.chameleon.mail.MailSenderService#send(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    public void send(String to, String cc, String subject, String body)
            throws Exception {
        send(to, cc, subject, body, null);
    }

    /**
     * Sends a mail
     * @param to to
     * @param cc cc
     * @param subject subject
     * @param body body
     * @param attachments list of attachments
     * @throws Exception if the mail cannot be sent
     * @see org.ow2.chameleon.mail.MailSenderService#send(java.lang.String, java.lang.String, java.lang.String,
     * java.lang.String, java.util.List)
     */
    public void send(String to, String cc, String subject, String body,
                     List<File> attachments) throws Exception {
        if (attachments != null  && ! attachments.isEmpty()) {
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
     * Sends the given mail object
     * @param mail the mail
     * @throws Exception if the mail cannot be sent.
     */
    public void send(Mail mail) throws Exception {
        if (mail.to() == null  || mail.to().isEmpty()) {
            throw new NullPointerException("The given 'to' is null or empty");
        }

        if (m_mock) {
            sendMessageWithMockServer(mail);
            return;
        }

        Session session = Session.getInstance(m_properties, sslAuthentication);

        session.setDebug(m_debug);
        // create a message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(m_from));

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
        if (attachments != null  && ! attachments.isEmpty()) {
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
        Transport transport = null;
        final ClassLoader original = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            if (m_useSMTPS) {
                transport = session.getTransport("smtps");
            } else {
                transport = session.getTransport("smtp");
            }
            if (m_connection == Connection.TLS) {
                transport.connect(m_host,
                        m_port, m_username, m_password);
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
        if (! mail.cc().isEmpty()) {
            LOGGER.info("\tCC: " + mail.cc());
        }
        LOGGER.info("\tSubject: " + mail.subject());
        LOGGER.info("\t----");
        LOGGER.info(mail.body());
        LOGGER.info("\t----");
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
