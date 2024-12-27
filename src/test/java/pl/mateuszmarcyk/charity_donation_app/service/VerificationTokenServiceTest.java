package pl.mateuszmarcyk.charity_donation_app.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.VerificationTokenRepository;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationTokenServiceTest {

    @InjectMocks
    private VerificationTokenService verificationTokenService;

    @Mock
    private VerificationTokenRepository verificationTokenRepository;

    @Mock
    private MessageSource messageSource;


    @Test
    void givenVerificationTokenService_whenSaveToken_thenVerificationTokenSaved() {
        User user = new User();
        user.setId(2L);
        VerificationToken verificationToken = new VerificationToken("token", user, 15);

        ArgumentCaptor<VerificationToken> verificationTokenArgumentCaptor = ArgumentCaptor.forClass(VerificationToken.class);
        verificationTokenService.saveToken(verificationToken);

        verify(verificationTokenRepository).save(verificationTokenArgumentCaptor.capture());
        VerificationToken savedToken = verificationTokenArgumentCaptor.getValue();

        assertThat(savedToken).isEqualTo(verificationToken);
    }

    @Test
    void givenVerificationTokenService_whenFindByToken_thenThrowTokenNotFoundException() {
        String token = "tokenExample";
        String tokenNotFoundTitle = "Test title";
        String tokenNotFoundMessage = "Test message";

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(verificationTokenRepository.findByToken(token)).thenReturn(Optional.empty());
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn(tokenNotFoundTitle);
        when(messageSource.getMessage("error.tokennotfound.message", null, Locale.getDefault())).thenReturn(tokenNotFoundMessage);

        assertThatThrownBy(() -> verificationTokenService.findByToken(token)).isInstanceOf(TokenNotFoundException.class).hasMessage("Test message");
        verify(verificationTokenRepository).findByToken(stringArgumentCaptor.capture());
        String tokenUsed = stringArgumentCaptor.getValue();

        assertThat(tokenUsed).isEqualTo(token);
        verify(messageSource, times(1)).getMessage("error.tokennotfound.title", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokennotfound.message", null, Locale.getDefault());
    }

    @Test
    void givenVerificationTokenService_whenFindByToken_thenTokenReturned() {
        User user = new User();
        user.setId(2L);
        VerificationToken verificationToken = new VerificationToken("token", user, 15);
        String tokenNotFoundTitle = "Test title";
        String tokenNotFoundMessage = "Test message";

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        when(verificationTokenRepository.findByToken(verificationToken.getToken())).thenReturn(Optional.of(verificationToken));
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn(tokenNotFoundTitle);
        when(messageSource.getMessage("error.tokennotfound.message", null, Locale.getDefault())).thenReturn(tokenNotFoundMessage);

        VerificationToken foundVerificationToken = verificationTokenService.findByToken(verificationToken.getToken());
        verify(verificationTokenRepository, times(1)).findByToken(stringArgumentCaptor.capture());

        String usedToken = stringArgumentCaptor.getValue();

        assertAll(
                () -> assertThat(usedToken).isEqualTo(verificationToken.getToken()),
                () -> assertThat(foundVerificationToken).isEqualTo(verificationToken)
        );
    }
}