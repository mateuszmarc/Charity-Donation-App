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
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.service.PasswordResetVerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.util.AppMailSender;
import pl.mateuszmarcyk.charity_donation_app.util.Mail;
import pl.mateuszmarcyk.charity_donation_app.util.MailMessage;
import pl.mateuszmarcyk.charity_donation_app.util.TokenFactory;
import pl.mateuszmarcyk.charity_donation_app.util.event.PasswordResetEvent;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetEventListenerTest {

    @InjectMocks
    private PasswordResetEventListener listener;

    @Mock
    private MessageSource messageSource;

    @Mock
    private AppMailSender appMailSender;

    @Mock
    private PasswordResetVerificationTokenService passwordResetVerificationTokenService;

    @Mock
    private MailMessage mailMessage;

    @Mock
    private TokenFactory tokenFactory;

    @Test
    void givenUserWithExpiredToken_whenEventPublished_thenEmailWithUpdatedTokenIsSent() throws MessagingException, UnsupportedEncodingException {
//        Arrange
        try (MockedStatic<UUID> uuidMockedStatic = mockStatic(UUID.class)) {
            String appName = "App name";
            String subject = "Test subject";
            String message = "Message";
            String token = "Randomtoken";
            int tokenValidTime = 15;
            String expectedUrl = "http://localhost/app/reset-password/verifyEmail?token=Randomtoken";
            UUID mockUUID = mock(UUID.class);
            User spyUser = spy(new User());
            String applicationUrl = "http://localhost/app";
            PasswordResetVerificationToken spyToken = spy(new PasswordResetVerificationToken(1L, "token", LocalDateTime.now().plusMinutes(15), spyUser, LocalDateTime.now(), false));
            spyUser.setPasswordResetVerificationToken(spyToken);

            uuidMockedStatic.when(UUID::randomUUID).thenReturn(mockUUID);

            when(mockUUID.toString()).thenReturn(token);
            when(messageSource.getMessage("token.valid.time", null, Locale.getDefault())).thenReturn("15");
            when(messageSource.getMessage("email.app.name", null, Locale.getDefault())).thenReturn(appName);
            when(messageSource.getMessage("registration.mail.subject", null, Locale.getDefault())).thenReturn(subject);
            when(mailMessage.buildPasswordResetMessage(expectedUrl)).thenReturn(message);
            PasswordResetEvent spyEvent = spy(new PasswordResetEvent(spyUser, applicationUrl));
            when(spyEvent.getApplicationUrl()).thenReturn(applicationUrl);

//            Act
            assertThatNoException().isThrownBy(() -> listener.onApplicationEvent(spyEvent));

//            Assert
            verify(spyEvent, times(1)).getUser();
            verify(messageSource, times(1)).getMessage("token.valid.time", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("email.app.name", null, Locale.getDefault());
            verify(messageSource, times(1)).getMessage("registration.mail.subject", null, Locale.getDefault());
            verify(spyUser, times(1)).getPasswordResetVerificationToken();

            ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
            verify(spyToken, times(1)).setToken(stringArgumentCaptor.capture());
            String capturedToken = stringArgumentCaptor.getValue();
            assertThat(capturedToken).isEqualTo(token);

            verify(tokenFactory, never()).getVerificationToken(token, spyUser, tokenValidTime);

            verify(spyToken, times(1)).setExpirationTime(any(LocalDateTime.class));

            ArgumentCaptor<PasswordResetVerificationToken> passwordResetVerificationTokenArgumentCaptor = ArgumentCaptor.forClass(PasswordResetVerificationToken.class);
            verify(passwordResetVerificationTokenService, times(1)).save(passwordResetVerificationTokenArgumentCaptor.capture());
            PasswordResetVerificationToken capturedPasswordResetVerificationToken = passwordResetVerificationTokenArgumentCaptor.getValue();
            assertThat(capturedPasswordResetVerificationToken).isEqualTo(spyToken);

            verify(spyEvent, times(1)).getApplicationUrl();

            verify(mailMessage, times(1)).buildPasswordResetMessage(stringArgumentCaptor.capture());
            String capturedUrl = stringArgumentCaptor.getValue();
            assertThat(capturedUrl).isEqualTo(expectedUrl);

            verify(appMailSender, times(1)).sendEmail(any(User.class), any(Mail.class));
        }
    }



}





















