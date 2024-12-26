package pl.mateuszmarcyk.charity_donation_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import pl.mateuszmarcyk.charity_donation_app.entity.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.PasswordResetVerificationTokenRepository;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetVerificationTokenServiceTest {

    @InjectMocks
    private PasswordResetVerificationTokenService service;

    @Mock
    private PasswordResetVerificationTokenRepository passwordResetVerificationTokenRepository;

    @Mock
    private MessageSource messageSource;


    @Test
    void whenSave_thenRepositorySaveMethodInvoked() {
        PasswordResetVerificationToken verificationToken = new PasswordResetVerificationToken("token", null, 15);

        ArgumentCaptor<PasswordResetVerificationToken> argumentCaptor = ArgumentCaptor.forClass(PasswordResetVerificationToken.class);
        service.save(verificationToken);

        verify(passwordResetVerificationTokenRepository).save(argumentCaptor.capture());
        PasswordResetVerificationToken savedToken = argumentCaptor.getValue();

        assertThat(savedToken).isEqualTo(verificationToken);
    }

    @Test
    void whenFindByToken_thenThrowTokenNotFoundException() {
        String token = "ssd";
        when(passwordResetVerificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());
        when(messageSource.getMessage("error.tokennotfound.message", null, Locale.getDefault())).thenReturn("Link aktywacyjny jest uszkodzony. Spróbuj jeszcze raz.");
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn("Weryfikacja konta");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        assertThatThrownBy(() -> service.findByToken(token)).isInstanceOf(TokenNotFoundException.class).hasMessage("Link aktywacyjny jest uszkodzony. Spróbuj jeszcze raz.");
        verify(passwordResetVerificationTokenRepository, times(1)).findByToken(argumentCaptor.capture());
        String usedToken = argumentCaptor.getValue();

        assertThat(usedToken).isEqualTo(token);
    }

    @Test
    void whenFindByToken_thenFindByTokenInvokedAndTokenReturned() {
        PasswordResetVerificationToken verificationToken = new PasswordResetVerificationToken("token", null, 15);

        when(passwordResetVerificationTokenRepository.findByToken(verificationToken.getToken())).thenReturn(Optional.of(verificationToken));
        when(messageSource.getMessage("error.tokennotfound.message", null, Locale.getDefault())).thenReturn("Link aktywacyjny jest uszkodzony. Spróbuj jeszcze raz.");
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn("Weryfikacja konta");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        PasswordResetVerificationToken foundToken = service.findByToken(verificationToken.getToken());

        verify(passwordResetVerificationTokenRepository, times(1)).findByToken(argumentCaptor.capture());

        String tokenToFind = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(tokenToFind).isEqualTo(verificationToken.getToken()),
                () -> assertThat(foundToken).isEqualTo(verificationToken)
        );
    }
}