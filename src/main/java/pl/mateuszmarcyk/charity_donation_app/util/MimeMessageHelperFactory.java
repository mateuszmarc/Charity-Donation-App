package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class MimeMessageHelperFactory {
    public MimeMessageHelper createHelper(MimeMessage mimeMessage, String from, String senderName, String subject, String content)
            throws MessagingException, UnsupportedEncodingException {
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        helper.setFrom(from, senderName);
        helper.setSubject(subject);
        helper.setText(content, true);
        return helper;
    }
}
