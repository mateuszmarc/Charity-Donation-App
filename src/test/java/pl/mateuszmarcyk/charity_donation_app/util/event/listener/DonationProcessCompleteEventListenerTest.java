package pl.mateuszmarcyk.charity_donation_app.util.event.listener;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;
import pl.mateuszmarcyk.charity_donation_app.util.event.DonationProcessCompleteEvent;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationProcessCompleteEventListenerTest {

    @InjectMocks
    private DonationProcessCompleteEventListener donationProcessCompleteEventListener;

    @Mock
    private MessageSource messageSource;

    @Mock
    private AppMailSender appMailSender;


    @Test
    void onApplicationEvent() throws MessagingException, UnsupportedEncodingException {
        try (MockedStatic<MailMessage> mailMessageMockedStatic = mockStatic(MailMessage.class)) {
            User spyUser = spy(User.class);
            spyUser.setProfile(new UserProfile());
            Institution institution = new Institution();
            Category category = new Category();
            Donation spyDonation = spy(getDonation(spyUser, institution, category));

            when(messageSource.getMessage(eq("email.app.name"), isNull(), any(Locale.class))).thenReturn("Charity App");
            when(messageSource.getMessage(eq("donation.subject"), isNull(), any(Locale.class))).thenReturn("Donation Complete");
            mailMessageMockedStatic.when(() -> MailMessage.buildDonationMessage(spyDonation)).thenReturn("Donation message");

            DonationProcessCompleteEvent event = spy(new DonationProcessCompleteEvent(spyDonation, spyUser));

            donationProcessCompleteEventListener.onApplicationEvent(event);

            verify(event, times(1)).getDonation();
            verify(event, times(1)).getUser();
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("donation.subject", null, Locale.getDefault());
            verify(appMailSender, times(1)).sendEmail(any(User.class), any(Mail.class));

            mailMessageMockedStatic.verify(() -> MailMessage.buildDonationMessage(spyDonation), times(1));
        }
    }

    @Test
    void givenMailException_whenHandled_thenRuntimeExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        try (MockedStatic<MailMessage> mailMessageMockedStatic = mockStatic(MailMessage.class)) {

            User spyUser = spy(User.class);
            spyUser.setProfile(new UserProfile());
            Institution institution = new Institution();
            Category category = new Category();

            Donation spyDonation = getDonation(spyUser, institution, category);
            when(messageSource.getMessage(anyString(), isNull(), any(Locale.class))).thenReturn("Some Message");
            mailMessageMockedStatic.when(() -> MailMessage.buildDonationMessage(spyDonation)).thenReturn("Donation message");

            doThrow(new MessagingException("Mail error")).when(appMailSender).sendEmail(any(User.class), any(Mail.class));

            DonationProcessCompleteEvent event = new DonationProcessCompleteEvent(spyDonation, spyUser);

            assertThrows(RuntimeException.class, () -> donationProcessCompleteEventListener.onApplicationEvent(event));

            mailMessageMockedStatic.verify(() -> MailMessage.buildDonationMessage(spyDonation), times(1));
            verify(appMailSender, times(1)).sendEmail(any(User.class), any(Mail.class));
        }
    }

    @Test
    void givenUnsupportedEncodingException_whenHandled_thenRuntimeExceptionThrown() throws MessagingException, UnsupportedEncodingException {
        try (MockedStatic<MailMessage> mailMessageMockedStatic = mockStatic(MailMessage.class)) {

            User spyUser = spy(User.class);
            spyUser.setProfile(new UserProfile());
            Institution institution = new Institution();
            Category category = new Category();

            Donation spyDonation = getDonation(spyUser, institution, category);
            when(messageSource.getMessage(anyString(), isNull(), any(Locale.class))).thenReturn("Some Message");

            doThrow(new UnsupportedEncodingException("Mail error")).when(appMailSender).sendEmail(any(User.class), any(Mail.class));

            DonationProcessCompleteEvent event = new DonationProcessCompleteEvent(spyDonation, spyUser);

            assertThrows(RuntimeException.class, () -> donationProcessCompleteEventListener.onApplicationEvent(event));

            mailMessageMockedStatic.verify(() -> MailMessage.buildDonationMessage(spyDonation), times(1));
            verify(appMailSender, times(1)).sendEmail(any(User.class), any(Mail.class));
        }
    }

    private static Donation getDonation(User user, Institution institution, Category category) {
        return new Donation(
                LocalDateTime.parse("2024-12-24T12:00:00"),
                false,
                user,
                institution,
                new ArrayList<>(List.of(category)),
                "123456789",
                "Please call on arrival.",
                LocalTime.parse("10:30:00"),
                LocalDate.now().plusDays(5),
                "12-345",
                "Kindness City",
                "123 Charity Lane",
                5
        );
    }
}