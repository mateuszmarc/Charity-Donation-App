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
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
        User user = getUser();
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
        User user = getUser();
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
        User user = getUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

//        Act & Assert
        UserDetails customUserDetails = customUserDetailsService.loadUserByUsername(email);
        if (customUserDetails instanceof CustomUserDetails details) {
            assertThat(details.getUser()).isEqualTo(user);

        }
    }


    private static User getUser() {
        UserProfile userProfile = new UserProfile(2L, null, "Mateusz", "Marcykiewicz", "Kielce",
                "Poland", null, "555666777");
        UserType userType = new UserType(2L, "ROLE_USER", new ArrayList<>());
        User user = new User(
                1L,
                "test@email.com",
                true,
                false,
                "testPW1!",
                LocalDateTime.of(2023, 11, 11, 12, 25, 11),
                "testPW1!",
                new HashSet<>(Set.of(userType)),
                userProfile,
                null,
                null,
                new ArrayList<>()
        );

        userProfile.setUser(user);
        return user;
    }
}