package pl.mateuszmarcyk.charity_donation_app.util.event.listener;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.service.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.util.*;
import pl.mateuszmarcyk.charity_donation_app.util.event.RegistrationCompleteEvent;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class RegistrationCompleteEventListenerTest {

    @InjectMocks
    private RegistrationCompleteEventListener listener;

    @Mock
    private  MessageSource messageSource;

    @Mock
    private  AppMailSender appMailSender;

    @Mock
    private  VerificationTokenService verificationTokenService;

    @Mock
    private  MailMessage mailMessage;

    @Mock
    private  TokenFactory tokenFactory;

    @Mock
    private MailFactory mailFactory;

    @Test
    void givenRegistrationCompleteEvent_whenOnApplicationEvent_thenRegistrationMailIsSent() throws MessagingException, UnsupportedEncodingException {
//        Assert
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String token = "Randomtoken";
            int tokenValidTime = 15;
            String expectedUrl = "http://localhost/app/register/verifyEmail?token=Randomtoken";
            UUID mockUUID = mock(UUID.class);
            User spyUser = spy(new User());
            String applicationUrl = "http://localhost/app";
            VerificationToken spyVerificationToken = spy(new VerificationToken(1L, "token", LocalDateTime.now().plusMinutes(15), spyUser, LocalDateTime.now()));
            Mail mail = new Mail(subject, appName, message);

            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);

            when(mockUUID.toString()).thenReturn(token);
            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            when(tokenFactory.getVerificationToken(token, spyUser, tokenValidTime)).thenReturn(spyVerificationToken);
            when(mailMessage.buildMessage(expectedUrl)).thenReturn(message);
            when(mailFactory.createMail(subject, appName, message)).thenReturn(mail);
            RegistrationCompleteEvent spyEvent = spy(new RegistrationCompleteEvent(spyUser, applicationUrl));
            when(spyEvent.getApplicationUrl()).thenReturn(applicationUrl);

//            Act
            assertThatNoException().isThrownBy(() -> listener.onApplicationEvent(spyEvent));

//            Verify
            verify(spyEvent, times(1)).getUser();

            uuidMockedStatic.verify(UUID::randomUUID, times(1));

            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<Integer> integerArgumentCaptor = ArgumentCaptor.forClass(Integer.class);
            verify(tokenFactory, times(1)).getVerificationToken(stringArgumentCaptor.capture(), userArgumentCaptor.capture(), integerArgumentCaptor.capture());
            String capturedToken = stringArgumentCaptor.getValue();
            User capturedUser = userArgumentCaptor.getValue();
            Integer capturedValidTime = integerArgumentCaptor.getValue();

            assertAll(
                    () -> assertThat(capturedToken).isEqualTo(token),
                    () -> assertThat(capturedUser).isEqualTo(spyUser),
                    () -> assertThat(capturedValidTime).isEqualTo(tokenValidTime)
            );

            ArgumentCaptor<VerificationToken> verificationTokenArgumentCaptor = ArgumentCaptor.forClass(VerificationToken.class);
            verify(verificationTokenService, times(1)).saveToken(verificationTokenArgumentCaptor.capture());
            VerificationToken capturedVerificationToken = verificationTokenArgumentCaptor.getValue();
            assertThat(capturedVerificationToken).isEqualTo(spyVerificationToken);

            verify(mailMessage, times(1)).buildMessage(stringArgumentCaptor.capture());
            String capturedApplicationUrl = stringArgumentCaptor.getValue();
            assertThat(capturedApplicationUrl).isEqualTo(expectedUrl);

            verify(mailFactory, times(1)).createMail(stringArgumentCaptor.capture(), stringArgumentCaptor.capture(), stringArgumentCaptor.capture());
            List<String> capturedArguments = stringArgumentCaptor.getAllValues();

            assertAll(
                    () -> assertThat(capturedArguments.get(2)).isEqualTo(subject),
                    () -> assertThat(capturedArguments.get(3)).isEqualTo(appName),
                    () -> assertThat(capturedArguments.get(4)).isEqualTo(message)
            );

            ArgumentCaptor<Mail> mailArgumentCaptor = ArgumentCaptor.forClass(Mail.class);
            verify(appMailSender, times(1)).sendEmail(userArgumentCaptor.capture(), mailArgumentCaptor.capture());

            User capturedUserForAppMailSender = userArgumentCaptor.getAllValues().get(1);
            Mail capturedMail = mailArgumentCaptor.getValue();

            assertAll(
                    () -> assertThat(capturedUserForAppMailSender).isEqualTo(spyUser),
                    () -> assertThat(capturedMail).isEqualTo(mail)
            );
        }
    }
}