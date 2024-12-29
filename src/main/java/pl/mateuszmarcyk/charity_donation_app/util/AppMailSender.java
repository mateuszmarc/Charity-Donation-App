package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.io.UnsupportedEncodingException;

@Component
public class AppMailSender {

    private final JavaMailSender mailSender;
    private final String appEmail;

    @Autowired
    public AppMailSender(JavaMailSender mailSender, @Value("${spring.mail.username}") String appEmail) {
        this.mailSender = mailSender;
        this.appEmail = appEmail;
    }

    public void sendEmail(User user, Mail mail) throws MessagingException, UnsupportedEncodingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        var messageHelper = getMimeMessageHelper(mimeMessage, mail);

        messageHelper.setTo(user.getEmail());
        mailSender.send(mimeMessage);
    }

    public void sendMailMessage(Mail mail) throws MessagingException, UnsupportedEncodingException {

        MimeMessage mimeMessage = mailSender.createMimeMessage();
        var messageHelper = getMimeMessageHelper(mimeMessage, mail);

        messageHelper.setTo(appEmail);

        mailSender.send(mimeMessage);
    }

    private MimeMessageHelper getMimeMessageHelper(MimeMessage mimeMessage, Mail mail) throws MessagingException, UnsupportedEncodingException {
        String subject = mail.getSubject();
        String senderName = mail.getSenderName();
        String mailContent = mail.getMailContent();

        var messageHelper = new MimeMessageHelper(mimeMessage,true, "UTF-8");
        messageHelper.setFrom(appEmail, senderName);

        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        return messageHelper;
    }
}
