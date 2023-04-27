package blankspace.blankspaceprj.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


public class MailConfig {

    @Value("${spring.mail.host}")
    String springMailHost;

    @Value("${spring.mail.port}")
    int springMailPort;



    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

        javaMailSender.setProtocol("smtp");
        javaMailSender.setHost(springMailHost);
        javaMailSender.setPort(springMailPort);

        return javaMailSender;
    }
}
