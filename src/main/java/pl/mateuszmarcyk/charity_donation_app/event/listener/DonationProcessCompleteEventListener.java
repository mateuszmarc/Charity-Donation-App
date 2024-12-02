package pl.mateuszmarcyk.charity_donation_app.event.listener;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.donation.Donation;
import pl.mateuszmarcyk.charity_donation_app.event.DonationProcessCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
@Component
public class DonationProcessCompleteEventListener implements ApplicationListener<DonationProcessCompleteEvent> {

    @Value("${email.app.name}")
    private String applicationName;

    @Value("${donation.subject}")
    private String donationSubject;

    private final AppMailSender appMailSender;

    @Override
    public void onApplicationEvent(DonationProcessCompleteEvent event) {

        Donation donation = event.getDonation();
        User user = event.getUser();
        String donationMessage = MailMessage.buildDonationMessage(donation);

        Mail mail = new Mail(donationSubject, applicationName, donationMessage);

        try {
            appMailSender.sendEmail(user, mail);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }
}
