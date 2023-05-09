package blankspace.blankspaceprj.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.host}")
    String springMailHost;

    @Value("${spring.mail.port}")
    int springMailPort;

    @Value("${spring.mail.properties.mail.smtp.starttls.enable}")
    boolean starttlsEnable;

    @Value("${spring.mail.properties.mail.smtp.auth}")
    boolean smtpAuth;

    @Value("${spring.mail.properties.mail.debug}")
    boolean smtpDebug;

    @Value("${spring.mail.transport.protocol}")
    String protocol;

    @Value("${spring.mail.username}")
    String senderUsername;

    @Value("${spring.mail.password}")
    String senderPassword;



    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        Properties properties = new Properties();
        properties.put("mail.transport.protocol", protocol);
        properties.put("mail.smtp.host", springMailHost);
        properties.put("mail.smtp.port", springMailPort);

        properties.put("mail.smtp.auth", smtpAuth);
        properties.put("mail.smtp.starttls.enable", starttlsEnable);
        properties.put("mail.smtp.debug", smtpDebug);
        properties.put("mail.smtp.user", senderUsername);
        properties.put("mail.smtp.password", senderPassword);


        Session session = Session.getDefaultInstance(properties,
                new javax.mail.Authenticator(){
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                senderUsername, senderPassword);// Specify the Username and the PassWord
                    }
                });
//        SmtpAuthenticator authentication = new SmtpAuthenticator();
//        javax.mail.Message msg = new MimeMessage(Session
//                .getDefaultInstance(properties, smtpAuthenticator));

        javaMailSender.setProtocol("smtp");
        javaMailSender.setHost(springMailHost);
        javaMailSender.setPort(springMailPort);
        javaMailSender.setJavaMailProperties(properties);

        return javaMailSender;
    }
}
