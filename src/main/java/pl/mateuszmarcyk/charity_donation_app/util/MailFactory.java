package pl.mateuszmarcyk.charity_donation_app.util;

import org.springframework.stereotype.Component;

@Component
public class MailFactory {

    public Mail createMail(String subject, String applicationName, String message) {
        return new Mail(subject,  applicationName, message);
    }
}
