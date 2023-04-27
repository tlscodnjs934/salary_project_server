package blankspace.blankspaceprj.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    String springMailUsername;

    public void mailSend(HashMap param) throws MessagingException {
        MimeMessage mail = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mail, false, "UTF-8"); // 2번째 인자는 Multipart여부 결정
        mimeMessageHelper.setFrom(springMailUsername);
        mimeMessageHelper.setTo((String) param.get("EMAIL"));
        mimeMessageHelper.setSubject("끌린더에서 발송한 코드입니다.");
        mimeMessageHelper.setText("끌린더에서 발송한 코드는 " + param.get("EMAIL_AUTH_CODE") + " 입니다.", true); // 2번째 인자는 HTML여부 결정
        javaMailSender.send(mail);
    }
}