package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.user.User;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Component
public class RegistrationMailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String appEmail;

    public void sendVerificationEmail(User user, String url) throws MessagingException, UnsupportedEncodingException {
        String subject = "Email Verification";

        String senderName = "Donation App";

        String mailContent = "<p> Hi,</p>" +
                "<p>Thank you for registering with us." + "<br/>" +
                "Please, follow the link below to complete your registration.</p>" + "<br/>" +
                "<a href=\"" + url + "\">Verify your email to activate your account</a>" + "<br/>" +
                "<p> Thank you <br> Users Registration Portal Service";

        MimeMessage message = mailSender.createMimeMessage();
        var messageHelper = new MimeMessageHelper(message);
        messageHelper.setFrom(appEmail, senderName);
        messageHelper.setTo(user.getEmail());
        messageHelper.setSubject(subject);
        messageHelper.setText(mailContent, true);
        mailSender.send(message);
    }

}
