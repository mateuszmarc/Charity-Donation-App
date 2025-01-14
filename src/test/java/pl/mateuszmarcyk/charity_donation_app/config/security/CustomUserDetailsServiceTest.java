package pl.mateuszmarcyk.charity_donation_app.config.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.ErrorMessages;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    private User user;

    private String email;

    @BeforeEach
    void setUpTestData() {
        user = TestDataFactory.getUser();
        email = "email@gmail.com";
    }

    @Test
    void whenLoadUserByUsernameAndNoUserInDatabase_thenUsernameNotFoundExceptionThrown() {
//        Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

//        Act & Assert
        assertExceptionAndMessage(email, UsernameNotFoundException.class,  ErrorMessages.USERNAME_NOT_FOUND_EXCEPTION_MESSAGE);
        verifyUserRepositoryUsage(email);
    }


    @Test
    void whenLoadByUsernameAndUserNoEnabled_thenDisabledExceptionThrown() {
        //        Arrange
        user.setEnabled(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//        Act & Assert
        assertExceptionAndMessage(email, DisabledException.class, ErrorMessages.DISABLED_EXCEPTION_MESSAGE);
        verifyUserRepositoryUsage(email);
    }

    @Test
    void whenLoadByUsernameAndUserBlocked_thenLockedExceptionThrown() {
        //        Arrange
        user.setBlocked(true);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//        Act & Assert
        assertExceptionAndMessage(email, LockedException.class, ErrorMessages.LOCKED_EXCEPTION_MESSAGE);
        verifyUserRepositoryUsage(email);
    }

    @Test
    void whenLoadByUsernameAndUserValid_thenCustomUserDetailsReturned() {
        //        Arrange
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//        Act & Assert
        UserDetails customUserDetails = customUserDetailsService.loadUserByUsername(email);
        if (customUserDetails instanceof CustomUserDetails details) {
            assertThat(details.getUser()).isEqualTo(user);
        }
        verifyUserRepositoryUsage(email);
    }

    private void assertExceptionAndMessage(String email, Class<? extends Exception> exceptionClass, String expectedExceptionMessage) {
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(exceptionClass)
                .hasMessage(expectedExceptionMessage);
    }

    private void verifyUserRepositoryUsage(String email) {
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository, times(1)).findByEmail(stringArgumentCaptor.capture());
        String capturedEmail = stringArgumentCaptor.getValue();
        assertThat(capturedEmail).isEqualTo(email);
    }
}