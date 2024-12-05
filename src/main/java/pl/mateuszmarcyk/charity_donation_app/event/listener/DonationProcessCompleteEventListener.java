package pl.mateuszmarcyk.charity_donation_app.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.donation.Donation;
import pl.mateuszmarcyk.charity_donation_app.event.DonationProcessCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

@RequiredArgsConstructor
@Component
public class DonationProcessCompleteEventListener implements ApplicationListener<DonationProcessCompleteEvent> {

    private final MessageSource messageSource;
    private final AppMailSender appMailSender;

    @Override
    public void onApplicationEvent(DonationProcessCompleteEvent event) {

        Donation donation = event.getDonation();
        User user = event.getUser();
        String donationMessage = MailMessage.buildDonationMessage(donation);
        String applicationName = messageSource.getMessage("email.app.name", null, Locale.getDefault());
        String donationSubject = messageSource.getMessage("donation.subject", null, Locale.getDefault());

        Mail mail = new Mail(donationSubject, applicationName, donationMessage);

        try {
            appMailSender.sendEmail(user, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }
}
