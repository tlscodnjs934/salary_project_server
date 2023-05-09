package blankspace.blankspaceprj.service;

import com.sun.mail.smtp.SMTPMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class MailService {

    @Value("${spring.mail.username}")
    String springMailUsername;

    @Value("${spring.mail.password}")
    String springMailPassword;

    @Autowired
    JavaMailSender javaMailSender;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public boolean mailSend(HashMap param) {
        SimpleMailMessage mimeMessageHelper = new SimpleMailMessage();

        Message m = javaMailSender.createMimeMessage();

        try {
            m.setSubject("끌린더에서 발송한 코드입니다.");
            m.setText("끌린더에서 발송한 코드는 " + param.get("EMAIL_AUTH_CODE") + " 입니다.");

            Address address = new InternetAddress((String) param.get("EMAIL"));

            m.setRecipient(Message.RecipientType.TO, address);
        } catch (MessagingException e) {
            e.printStackTrace();
            logger.error("이메일 메시지 세팅 중 오류 발생 : + " + e.getMessage());
            return false;
        }

        //발신자
        mimeMessageHelper.setFrom(springMailUsername);

        //수신자
        mimeMessageHelper.setTo((String) param.get("EMAIL"));
        mimeMessageHelper.setSubject("끌린더에서 발송한 코드입니다.");
        mimeMessageHelper.setText("끌린더에서 발송한 코드는 " + param.get("EMAIL_AUTH_CODE") + " 입니다.");

        try {
            //javaMailSender.send(mimeMessageHelper);
            Transport.send(m, springMailUsername, springMailPassword);
        }catch (Exception e){
            e.printStackTrace();
            logger.error("메일 발송 실패 : " + param);
            return false;
        }

        logger.info("메일 발송 성공 : " + param);
        return true;
    }
}