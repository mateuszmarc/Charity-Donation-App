package pl.mateuszmarcyk.charity_donation_app.event.listener;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.event.RegistrationCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.user.User;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class RegistrationCompleteEventListener implements ApplicationListener<RegistrationCompleteEvent> {

    @Value("${token.valid.time}")
    private int tokenValidTime;

    @Value("${spring.mail.username}")
    private String appEmail;

    private final VerificationTokenService verificationTokenService;
    private final JavaMailSender mailSender;

    @Override
    public void onApplicationEvent(RegistrationCompleteEvent event) {

        User user = event.getUser();
        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken(token, user, tokenValidTime);
        verificationTokenService.saveToken(verificationToken);

        String url = event.getApplicationUrl() + "/register/verifyEmail?token=" + token;

        try {
            sendVerificationEmail(user, url);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendVerificationEmail(User user, String url) throws MessagingException, UnsupportedEncodingException {
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
