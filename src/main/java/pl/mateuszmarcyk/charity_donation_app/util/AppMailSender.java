package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Component
public class AppMailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String appEmail;

    public void sendEmail(User user, Mail mail) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = getMimeMessageHelper(message, mail);

        messageHelper.setTo(user.getEmail());
        mailSender.send(message);
    }

    public void sendMailMessage(Mail mail) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = getMimeMessageHelper(message, mail);

        messageHelper.setTo(appEmail);

        mailSender.send(message);
    }

    private MimeMessageHelper getMimeMessageHelper(MimeMessage message, Mail mail) throws MessagingException, UnsupportedEncodingException {
        String subject = mail.getSubject();
        String senderName = mail.getSenderName();
        String mailContent = mail.getMailContent();

        var messageHelper = new MimeMessageHelper(message,true, "UTF-8");
        messageHelper.setFrom(appEmail, senderName);

        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        return messageHelper;
    }
}
