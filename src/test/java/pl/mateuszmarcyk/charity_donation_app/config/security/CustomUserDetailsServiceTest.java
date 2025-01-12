package pl.mateuszmarcyk.charity_donation_app.config.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.TestDataFactory;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    void whenLoadUserByUsernameAndNoUserInDatabase_thenUsernameNotFoundExceptionThrown() {
//        Arrange
        String email = "email@gmail.com";
        String expectedExceptionMessage = "Could not find the user";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

//        Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage(expectedExceptionMessage);
    }

    @Test
    void whenLoadByUsernameAndUserNoEnabled_thenDisabledExceptionThrown() {
        //        Arrange
        String email = "email@gmail.com";
        User user = TestDataFactory.getUser();
        user.setEnabled(false);
        String expectedExceptionMessage = "User is not enabled";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//        Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(DisabledException.class)
                .hasMessage(expectedExceptionMessage);
    }

    @Test
    void whenLoadByUsernameAndUserBlocked_thenLockedExceptionThrown() {
        //        Arrange
        String email = "email@gmail.com";
        User user = TestDataFactory.getUser();
        user.setBlocked(true);
        String expectedExceptionMessage = "User is blocked";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//        Act & Assert
        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(email))
                .isInstanceOf(LockedException.class)
                .hasMessage(expectedExceptionMessage);
    }

    @Test
    void whenLoadByUsernameAndUserValid_thenCustomUserDetailsReturned() {
        //        Arrange
        String email = "email@gmail.com";
        User user = TestDataFactory.getUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//        Act & Assert
        UserDetails customUserDetails = customUserDetailsService.loadUserByUsername(email);
        if (customUserDetails instanceof CustomUserDetails details) {
            assertThat(details.getUser()).isEqualTo(user);
        }
    }
}