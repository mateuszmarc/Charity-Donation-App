package pl.mateuszmarcyk.charity_donation_app.util.event.listener;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;
import pl.mateuszmarcyk.charity_donation_app.util.event.DonationProcessCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.util.MailFactory;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
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

    @Mock
    private MailMessage mailMessage;

    @Mock
    private MailFactory mailFactory;


    @Test
    void givenDonationProcessCompleteEvent_whenOnApplicationEvent_thenMailIsSent() throws MessagingException, UnsupportedEncodingException {
//        Arrange
        String appName = "App name";
        String subject = "Test subject";
        String message = "Donation message";

        Mail mail = new Mail(subject, appName, message);
        User spyUser = spy(User.class);
        Institution spyInstitution = spy(Institution.class);
        Category spyCategory = spy(Category.class);
        Donation spyDonation = getDonation(spyUser, spyInstitution, spyCategory);

        when(mailMessage.buildDonationMessage(spyDonation)).thenReturn(message);
        when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
        when(messageSource.getMessage("donation.subject", null, Locale.getDefault())).thenReturn(subject);
        when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);
        DonationProcessCompleteEvent spyEvent = spy(new DonationProcessCompleteEvent(spyDonation, spyUser));

//        Act
        donationProcessCompleteEventListener.onApplicationEvent(spyEvent);

//        Assert
        verify(spyEvent, times(1)).getDonation();
        verify(spyEvent, times(1)).getUser();

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(mailMessage, times(1)).buildDonationMessage(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();
        assertThat(capturedDonation).isEqualTo(spyDonation);

        verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("donation.subject", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        assertIterableEquals(List.of(subject, appName, message), stringArgumentCaptor.getAllValues());

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);

        verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        Mail capturedMail = mailArgumentCaptor.getValue();


        assertAll(
                () -> assertThat(capturedUser).isEqualTo(spyUser),
                () -> assertThat(capturedMail).isEqualTo(mail)
        );
    }


    @Test
    void givenMailException_whenHandled_thenRuntimeExceptionThrown() throws MessagingException, UnsupportedEncodingException {
//        Arrange
        String appName = "App name";
        String subject = "Test subject";
        String message = "Donation message";

        Mail mail = new Mail(subject, appName, message);
        User spyUser = spy(User.class);
        Institution spyInstitution = spy(Institution.class);
        Category spyCategory = spy(Category.class);
        Donation spyDonation = getDonation(spyUser, spyInstitution, spyCategory);

        when(mailMessage.buildDonationMessage(spyDonation)).thenReturn(message);
        when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
        when(messageSource.getMessage("donation.subject", null, Locale.getDefault())).thenReturn(subject);
        when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);
        DonationProcessCompleteEvent spyEvent = spy(new DonationProcessCompleteEvent(spyDonation, spyUser));
        doThrow(new MessagingException("Mail error")).when(appMailSender).sendEmail(any(User.class), any(Mail.class));

        //      Act
        assertThrows(RuntimeException.class, () -> donationProcessCompleteEventListener.onApplicationEvent(spyEvent));

        //        Assert
        verify(spyEvent, times(1)).getDonation();
        verify(spyEvent, times(1)).getUser();

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(mailMessage, times(1)).buildDonationMessage(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();
        assertThat(capturedDonation).isEqualTo(spyDonation);

        verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("donation.subject", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        assertIterableEquals(List.of(subject, appName, message), stringArgumentCaptor.getAllValues());

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);

        verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        Mail capturedMail = mailArgumentCaptor.getValue();


        assertAll(
                () -> assertThat(capturedUser).isEqualTo(spyUser),
                () -> assertThat(capturedMail).isEqualTo(mail)
        );
    }

    @Test
    void givenUnsupportedEncodingException_whenHandled_thenRuntimeExceptionThrown() throws MessagingException, UnsupportedEncodingException {
//        Arrange
        String appName = "App name";
        String subject = "Test subject";
        String message = "Donation message";

        Mail mail = new Mail(subject, appName, message);
        User spyUser = spy(User.class);
        Institution spyInstitution = spy(Institution.class);
        Category spyCategory = spy(Category.class);
        Donation spyDonation = getDonation(spyUser, spyInstitution, spyCategory);

        when(mailMessage.buildDonationMessage(spyDonation)).thenReturn(message);
        when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
        when(messageSource.getMessage("donation.subject", null, Locale.getDefault())).thenReturn(subject);
        when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);
        DonationProcessCompleteEvent spyEvent = spy(new DonationProcessCompleteEvent(spyDonation, spyUser));
        doThrow(new UnsupportedEncodingException("Mail error")).when(appMailSender).sendEmail(any(User.class), any(Mail.class));

        //      Act
        assertThrows(RuntimeException.class, () -> donationProcessCompleteEventListener.onApplicationEvent(spyEvent));

        //        Assert
        verify(spyEvent, times(1)).getDonation();
        verify(spyEvent, times(1)).getUser();

        ArgumentCaptor<Donation> donationArgumentCaptor = ArgumentCaptor.forClass(Donation.class);
        verify(mailMessage, times(1)).buildDonationMessage(donationArgumentCaptor.capture());
        Donation capturedDonation = donationArgumentCaptor.getValue();
        assertThat(capturedDonation).isEqualTo(spyDonation);

        verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("donation.subject", null, Locale.getDefault());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
        assertIterableEquals(List.of(subject, appName, message), stringArgumentCaptor.getAllValues());

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);

        verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        Mail capturedMail = mailArgumentCaptor.getValue();


        assertAll(
                () -> assertThat(capturedUser).isEqualTo(spyUser),
                () -> assertThat(capturedMail).isEqualTo(mail)
        );
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