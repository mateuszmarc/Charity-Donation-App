package pl.mateuszmarcyk.charity_donation_app.service;

import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.data.Index;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.exception.*;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.Email;
import pl.mateuszmarcyk.charity_donation_app.util.event.PasswordResetEvent;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserTypeService userTypeService;

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private ApplicationEventPublisher publisher;

    @Mock
    private PasswordResetVerificationTokenService passwordResetVerificationTokenService;

    @Mock
    private MessageSource messageSource;

    @Test
    void givenUserService_whenSaveUser_thenUserIsSaved() {
        UserType userRole = new UserType(1L, "ROLE_ADMIN", new ArrayList<>());
        String plainPassword = "Password123!";
        String encodedPassword = "Encoded Password";
        User user = spy(User.class);
        user.setId(1L);
        user.setPassword(plainPassword);


        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<Long> userRoleIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> plainPasswordArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> encodedPasswordArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<UserType> userTypeArgumentCaptor = ArgumentCaptor.forClass(UserType.class);
        ArgumentCaptor<UserProfile> userProfileArgumentCaptor = ArgumentCaptor.forClass(UserProfile.class);

        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);
        when(userTypeService.findById(userRole.getId())).thenReturn(userRole);
        when(userRepository.save(user)).thenReturn(user);

        User savedUser = userService.save(user);

        verify(user, times(1)).getPassword();

        verify(passwordEncoder, times(1)).encode(plainPasswordArgumentCaptor.capture());
        String passwordArgumentToEncode = plainPasswordArgumentCaptor.getValue();
        assertThat(passwordArgumentToEncode).isEqualTo(plainPassword);

        verify(user, times(2)).setPassword(encodedPasswordArgumentCaptor.capture());
        String setEncodedPassword = encodedPasswordArgumentCaptor.getValue();
        assertThat(setEncodedPassword).isEqualTo(encodedPassword);

        verify(userTypeService, times(1)).findById(userRoleIdArgumentCaptor.capture());
        Long userRoleId = userRoleIdArgumentCaptor.getValue();
        assertThat(userRoleId).isEqualTo(userRole.getId());

        verify(user).addUserType(userTypeArgumentCaptor.capture());
        UserType addedUserType = userTypeArgumentCaptor.getValue();
        assertThat(addedUserType).isEqualTo(userRole);

        verify(user).setUserProfile(userProfileArgumentCaptor.capture());
        UserProfile setUserProfile = userProfileArgumentCaptor.getValue();
        assertThat(setUserProfile.getId()).isNull();

        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User userToSave = userArgumentCaptor.getValue();
        assertThat(userToSave).isEqualTo(user);
        assertThat(savedUser).isEqualTo(user);
    }


    @Test
    void givenUserService_whenFindUserByEmail_thenUsernameNotFoundExceptionThrown() {
        String email = "email.example@gmail.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        assertThatThrownBy(() -> userService.findUserByEmail(email)).isInstanceOf(UsernameNotFoundException.class).hasMessage("There is no such user");

        verify(userRepository).findByEmail(argumentCaptor.capture());
        String usedEmail = argumentCaptor.getValue();

        assertThat(usedEmail).isEqualTo(email);
    }

    @Test
    void givenUserService_whenFindUserByEmail_thenUserFound() {
        User user = new User();
        String email = "email.example@gmail.com";
        user.setEmail(email);
        user.setId(1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        User foundUser = userService.findUserByEmail(email);
        assertThat(foundUser).isEqualTo(user);

        verify(userRepository).findByEmail(argumentCaptor.capture());
        String usedEmail = argumentCaptor.getValue();
        assertThat(usedEmail).isEqualTo(email);

    }

    @Test
    void givenUserService_whenValidateTokenAndTokenNotInDatabase_thenTokenNotFoundExceptionThrownAndUserNoEnabled() {
        String token = "token";

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(verificationTokenService.findByToken(token)).thenThrow(new TokenNotFoundException("Token not found", "Token is not found"));

        assertThatThrownBy(() -> userService.validateToken(token)).isInstanceOf(TokenNotFoundException.class).hasMessage("Token is not found");
        verify(verificationTokenService).findByToken(argumentCaptor.capture());
        String usedToken = argumentCaptor.getValue();
        assertThat(usedToken).isEqualTo(token);
    }

    @Test
    void givenUserService_whenValidateTokenAndNotEnabledUserAndExpirationTimeNotPassed_thenUserEnabledAndUpdated() {
        User user = spy(User.class);
        LocalDateTime creationDateTime = LocalDateTime.now();
        LocalDateTime expirationDateTime = creationDateTime.plusMinutes(15);
        VerificationToken verificationToken = new VerificationToken(1L, "token", expirationDateTime, user, creationDateTime);
        VerificationToken spyVerificationToken = spy(verificationToken);

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> isEnabledArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(verificationTokenService.findByToken(verificationToken.getToken())).thenReturn(spyVerificationToken);

        assertThatNoException().isThrownBy(() -> userService.validateToken(spyVerificationToken.getToken()));

        verify(verificationTokenService).findByToken(argumentCaptor.capture());
        String usedToken = argumentCaptor.getValue();
        assertThat(usedToken).isEqualTo(spyVerificationToken.getToken());

        verify(messageSource, times(1)).getMessage("error.tokenconsumed.message", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokennotfound.title", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokenexpired.message", null, Locale.getDefault());

        verify(spyVerificationToken, times(2)).getUser();
        verify(user, times(1)).isEnabled();
        verify(spyVerificationToken, times(1)).getExpirationTime();

        verify(user).setEnabled(isEnabledArgumentCaptor.capture());
        Boolean isEnabled = isEnabledArgumentCaptor.getValue();
        assertThat(isEnabled).isTrue();

        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser).isEqualTo(user);
    }

    @Test
    void givenUserService_whenValidateTokenAndUserEnabled_thenTokenAlreadyConsumedExceptionThrownAndUserNotUpdated() {
        User user = new User();
        user.setEnabled(true);
        User spyUser = spy(user);
        LocalDateTime creationDateTime = LocalDateTime.now();
        LocalDateTime expirationDateTime = creationDateTime.plusMinutes(15);
        VerificationToken verificationToken = new VerificationToken(1L, "token", expirationDateTime, spyUser, creationDateTime);
        VerificationToken spyVerificationToken = spy(verificationToken);
        String tokenAlreadyConsumedMessage = "Token already consumed";

        when(verificationTokenService.findByToken(spyVerificationToken.getToken())).thenReturn(spyVerificationToken);
        when(messageSource.getMessage("error.tokenconsumed.message", null, Locale.getDefault())).thenReturn(tokenAlreadyConsumedMessage);

        assertThatThrownBy(() -> userService.validateToken(spyVerificationToken.getToken())).isInstanceOf(TokenAlreadyConsumedException.class).hasMessage(tokenAlreadyConsumedMessage);

        verify(messageSource, times(1)).getMessage("error.tokenconsumed.message", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokennotfound.title", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokenexpired.message", null, Locale.getDefault());

        verify(spyVerificationToken, times(1)).getUser();
        verify(spyUser, times(1)).isEnabled();

        verify(spyVerificationToken, times(1)).getUser();
        verify(spyVerificationToken, never()).getExpirationTime();
        verify(spyUser, never()).setEnabled(true);
        verify(userRepository, never()).save(spyUser);
    }

    @Test
    void givenUserService_whenValidateTokenAndUserEnabledAndTokenAlreadyExpired_thenTokenAlreadyConsumedExceptionThrownAndUserNotUpdated() {
        User user = new User();
        user.setEnabled(true);
        User spyUser = spy(user);
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 12, 12, 12, 12, 0);
        LocalDateTime expirationDateTime = creationDateTime.plusMinutes(15);
        VerificationToken verificationToken = new VerificationToken(1L, "token", expirationDateTime, spyUser, creationDateTime);
        VerificationToken spyVerificationToken = spy(verificationToken);
        String tokenAlreadyConsumedMessage = "Token already consumed";

        when(verificationTokenService.findByToken(spyVerificationToken.getToken())).thenReturn(spyVerificationToken);
        when(messageSource.getMessage("error.tokenconsumed.message", null, Locale.getDefault())).thenReturn(tokenAlreadyConsumedMessage);

        assertThatThrownBy(() -> userService.validateToken(spyVerificationToken.getToken())).isInstanceOf(TokenAlreadyConsumedException.class).hasMessage(tokenAlreadyConsumedMessage);

        verify(messageSource, times(1)).getMessage("error.tokenconsumed.message", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokennotfound.title", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokenexpired.message", null, Locale.getDefault());

        verify(spyVerificationToken, times(1)).getUser();
        verify(spyUser, times(1)).isEnabled();

        verify(spyVerificationToken, times(1)).getUser();
        verify(spyVerificationToken, never()).getExpirationTime();
        verify(spyUser, never()).setEnabled(true);
        verify(userRepository, never()).save(spyUser);
    }

    @Test
    void givenUserService_whenValidateTokenForNotEnabledUserAndTokenExpirationTimePassed_thenTokenAlreadyExpiredExceptionThrownAndUserNotUpdated() {
        User user = spy(User.class);
        LocalDateTime creationDateTime = LocalDateTime.of(2023, 12, 12, 12, 12, 0);
        LocalDateTime expirationDateTime = creationDateTime.plusMinutes(15);
        VerificationToken verificationToken = new VerificationToken(1L, "token", expirationDateTime, user, creationDateTime);
        VerificationToken spyVerificationToken = spy(verificationToken);
        String tokenAlreadyExpiredMessage = "Token already expired";
        String tokenAlreadyExpiredTitle = "Title Token already expired";

        when(verificationTokenService.findByToken(spyVerificationToken.getToken())).thenReturn(spyVerificationToken);
        when(messageSource.getMessage("error.tokenexpired.message", null, Locale.getDefault())).thenReturn(tokenAlreadyExpiredMessage);
        when(messageSource.getMessage("error.tokenconsumed.message", null, Locale.getDefault())).thenReturn("Token is consumed");
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn(tokenAlreadyExpiredTitle);


        Throwable thrown = catchThrowable(() -> userService.validateToken(spyVerificationToken.getToken()));
        assertThat(thrown).isInstanceOf(TokenAlreadyExpiredException.class);
        assertThat(thrown.getMessage()).isEqualTo(tokenAlreadyExpiredMessage);

        if (thrown instanceof TokenAlreadyExpiredException tokenAlreadyExpiredException) {
            assertAll(
                    () -> assertThat(tokenAlreadyExpiredException.getToken()).isEqualTo(spyVerificationToken.getToken()),
                    () -> assertThat(tokenAlreadyExpiredException.getTitle()).isEqualTo(tokenAlreadyExpiredTitle)
            );
        }

        verify(messageSource, times(1)).getMessage("error.tokenconsumed.message", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokennotfound.title", null, Locale.getDefault());
        verify(messageSource, times(1)).getMessage("error.tokenexpired.message", null, Locale.getDefault());

        verify(spyVerificationToken, times(1)).getUser();
        verify(user, times(1)).isEnabled();
        verify(spyVerificationToken, times(1)).getExpirationTime();
        verify(user, never()).setEnabled(true);
        verify(userRepository, never()).save(user);
    }


    @Test
    void givenUserService_whenValidatePasswordResetTokenAndTokenNotFound_thenThrowTokenNotFoundException() {
        String token = "token";
        String tokenNotFoundTitle = "Token not found";
        String tokenNotfoundMessage = "Could not find token";
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        when(passwordResetVerificationTokenService.findByToken(token)).thenThrow(new TokenNotFoundException("Token not found", "Could not find token"));

        Throwable thrown = catchThrowable(() -> userService.validatePasswordResetToken(token));
        assertThat(thrown).isInstanceOf(TokenNotFoundException.class);
        if (thrown instanceof TokenNotFoundException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(tokenNotFoundTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(tokenNotfoundMessage)
            );
        }

        verify(passwordResetVerificationTokenService).findByToken(argumentCaptor.capture());
        String usedToken = argumentCaptor.getValue();
        assertThat(usedToken).isEqualTo(token);
        verify(messageSource, never()).getMessage(any(), any(), any());
    }

    @Test
    void givenUserService_whenValidatePasswordResetTokenAndTokenIsConsumed_thenTokenAlreadyConsumedExceptionIsThrown() {
        User spyUser = spy(User.class);
        String token = "token";
        LocalDateTime creationDateTime = LocalDateTime.now();
        LocalDateTime expirationDateTime = creationDateTime.plusMinutes(15);
        PasswordResetVerificationToken passwordResetVerificationToken = new PasswordResetVerificationToken(1L, token, expirationDateTime, spyUser, creationDateTime, true);
        PasswordResetVerificationToken spyPasswordResetVerificationToken = spy(passwordResetVerificationToken);

        String tokenConsumedMessage = "Token already consumed";
        String tokenErrorTitle = "Validation token error";
        String tokenExpiredMessage = "Token is expired";

        ArgumentCaptor<String> tokenArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(passwordResetVerificationTokenService.findByToken(spyPasswordResetVerificationToken.getToken())).thenReturn(spyPasswordResetVerificationToken);
        when(messageSource.getMessage("error.tokenconsumed.message", null, Locale.getDefault())).thenReturn(tokenConsumedMessage);
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn(tokenErrorTitle);
        when(messageSource.getMessage("error.tokenexpired.message", null, Locale.getDefault())).thenReturn(tokenExpiredMessage);

        Throwable thrown = catchThrowable(() -> userService.validatePasswordResetToken(token));
        assertThat(thrown).isInstanceOf(TokenAlreadyConsumedException.class);
        if (thrown instanceof TokenAlreadyConsumedException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(tokenErrorTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(tokenConsumedMessage)
            );
        }

        verify(passwordResetVerificationTokenService).findByToken(tokenArgumentCaptor.capture());
        String usedToken = tokenArgumentCaptor.getValue();
        assertThat(usedToken).isEqualTo(token);

        verify(spyPasswordResetVerificationToken, times(1)).isConsumed();
        verify(spyPasswordResetVerificationToken, never()).getExpirationTime();
        verify(spyPasswordResetVerificationToken, never()).getUser();
    }

    @Test
    void givenUserService_thenValidatePasswordResetTokenAndTokenExpired_thenTokenAlreadyExpiredExceptionIsThrown() {
        User spyUser = spy(User.class);
        String token = "token";
        LocalDateTime creationDateTime = LocalDateTime.of(2024, 12, 12, 12, 12, 0);
        LocalDateTime expirationDateTime = creationDateTime.plusMinutes(15);
        PasswordResetVerificationToken passwordResetVerificationToken = new PasswordResetVerificationToken(1L, token, expirationDateTime, spyUser, creationDateTime, false);
        PasswordResetVerificationToken spyPasswordResetVerificationToken = spy(passwordResetVerificationToken);

        String tokenConsumedMessage = "Token already consumed";
        String tokenErrorTitle = "Validation token error";
        String tokenExpiredMessage = "Token is expired";

        ArgumentCaptor<String> tokenArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(passwordResetVerificationTokenService.findByToken(spyPasswordResetVerificationToken.getToken())).thenReturn(spyPasswordResetVerificationToken);
        when(messageSource.getMessage("error.tokenconsumed.message", null, Locale.getDefault())).thenReturn(tokenConsumedMessage);
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn(tokenErrorTitle);
        when(messageSource.getMessage("error.tokenexpired.message", null, Locale.getDefault())).thenReturn(tokenExpiredMessage);

        Throwable thrown = catchThrowable(() -> userService.validatePasswordResetToken(token));
        assertThat(thrown).isInstanceOf(TokenAlreadyExpiredException.class);
        if (thrown instanceof TokenAlreadyExpiredException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(tokenErrorTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(tokenExpiredMessage),
                    () -> assertThat(exception.getToken()).isEqualTo(token)
            );
        }

        verify(passwordResetVerificationTokenService).findByToken(tokenArgumentCaptor.capture());
        String usedToken = tokenArgumentCaptor.getValue();
        assertThat(usedToken).isEqualTo(token);

        verify(spyPasswordResetVerificationToken, times(1)).isConsumed();
        verify(spyPasswordResetVerificationToken, times(1)).getExpirationTime();
        verify(spyPasswordResetVerificationToken, never()).getUser();
    }

    @Test
    void givenUserService_whenValidatePasswordResetToken_thenUserIsReturned() {
        User spyUser = spy(User.class);
        String token = "token";
        LocalDateTime creationDateTime = LocalDateTime.now();
        LocalDateTime expirationDateTime = creationDateTime.plusMinutes(15);
        PasswordResetVerificationToken passwordResetVerificationToken = new PasswordResetVerificationToken(1L, token, expirationDateTime, spyUser, creationDateTime, false);
        PasswordResetVerificationToken spyPasswordResetVerificationToken = spy(passwordResetVerificationToken);

        String tokenConsumedMessage = "Token already consumed";
        String tokenErrorTitle = "Validation token error";
        String tokenExpiredMessage = "Token is expired";

        ArgumentCaptor<String> tokenArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(passwordResetVerificationTokenService.findByToken(token)).thenReturn(spyPasswordResetVerificationToken);
        when(messageSource.getMessage("error.tokenconsumed.message", null, Locale.getDefault())).thenReturn(tokenConsumedMessage);
        when(messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault())).thenReturn(tokenErrorTitle);
        when(messageSource.getMessage("error.tokenexpired.message", null, Locale.getDefault())).thenReturn(tokenExpiredMessage);

        User returnedUser = userService.validatePasswordResetToken(token);
        assertThat(returnedUser).isSameAs(spyUser);

        verify(passwordResetVerificationTokenService).findByToken(tokenArgumentCaptor.capture());
        String usedToken = tokenArgumentCaptor.getValue();
        assertThat(usedToken).isEqualTo(token);

        verify(spyPasswordResetVerificationToken, times(1)).isConsumed();
        verify(spyPasswordResetVerificationToken, times(1)).getExpirationTime();
        verify(spyPasswordResetVerificationToken, times(1)).getUser();
    }

    @Test
    void givenUserService_whenFindUserByVerificationTokenThenResourceNotFoundExceptionThrown() {
        String token = "token";

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(userRepository.findUserByVerificationToken_Token(token)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByVerificationToken(token)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Użytkownik nie istnieje");
        verify(userRepository).findUserByVerificationToken_Token(argumentCaptor.capture());
        String usedToken = argumentCaptor.getValue();
        assertThat(usedToken).isEqualTo(token);
    }

    @Test
    void givenUserService_whenFindUserByVerificationTokenThenUserFound() {
        User user = new User();
        user.setId(1L);
        String token = "token";

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(userRepository.findUserByVerificationToken_Token(token)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserByVerificationToken(token);

        verify(userRepository).findUserByVerificationToken_Token(argumentCaptor.capture());
        String usedToken = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(foundUser).isEqualTo(user),
                () -> assertThat(usedToken).isEqualTo(token)
        );
    }

    @Test
    void givenUserService_whenFindAllAdmins_thenAdminListIsReturnedWithoutAdminUserInvokingTheMethod() {
        String userRole = "ROLE_ADMIN";
        User adminUserOne = new User();
        adminUserOne.setId(1L);

        User adminUserTwo = new User();
        adminUserTwo.setId(2L);

        User adminUserThree = new User();
        adminUserThree.setId(3L);

        List<User> spyUserList = spy(new ArrayList<>(List.of(adminUserOne, adminUserTwo, adminUserThree)));

        ArgumentCaptor<String> roleArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findUsersByRoleNative(userRole)).thenReturn(spyUserList);

        List<User> allAdminUsers = userService.findAllAdmins(adminUserOne);
        assertThat(allAdminUsers).hasSize(2);
        assertAll(
                () -> assertThat(allAdminUsers).contains(adminUserTwo, Index.atIndex(0)),
                () -> assertThat(allAdminUsers).contains(adminUserThree, Index.atIndex(1)),
                () -> assertThat(allAdminUsers).doesNotContain(adminUserOne)
        );

        verify(userRepository).findUsersByRoleNative(roleArgumentCaptor.capture());
        String usedUserRole = roleArgumentCaptor.getValue();
        assertThat(usedUserRole).isEqualTo(userRole);
    }

    @Test
    void givenUserService_whenFindAllAdminsAndOnlyOneAdminInDatabase_thenAdminListIsReturnedEmptyWithoutAdminUserInvokingTheMethod() {
        String userRole = "ROLE_ADMIN";
        User adminUserOne = new User();
        adminUserOne.setId(1L);

        List<User> spyUserList = spy(new ArrayList<>(List.of(adminUserOne)));

        ArgumentCaptor<String> roleArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findUsersByRoleNative(userRole)).thenReturn(spyUserList);

        List<User> allAdminUsers = userService.findAllAdmins(adminUserOne);
        assertThat(allAdminUsers).isEmpty();

        verify(userRepository).findUsersByRoleNative(roleArgumentCaptor.capture());
        String usedUserRole = roleArgumentCaptor.getValue();
        assertThat(usedUserRole).isEqualTo(userRole);
    }

    @Test
    void givenUserService_whenFindUserById_thenResourceNotFoundExceptionThrown() {
        Long id = 1L;

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Użytkownik nie istnieje");
        verify(userRepository).findById(argumentCaptor.capture());
        Long usedId = argumentCaptor.getValue();
        assertThat(usedId).isEqualTo(id);
    }

    @Test
    void givenUserService_whenFindUserById_thenUserFound() {
        Long id = 1L;
        User user = new User();
        user.setId(id);

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User foundUser = userService.findUserById(id);

        verify(userRepository).findById(argumentCaptor.capture());
        Long usedId = argumentCaptor.getValue();

        assertAll(
                () -> assertThat(foundUser).isEqualTo(user),
                () -> assertThat(usedId).isEqualTo(id)
        );
    }

    @Test
    void givenUserService_whenUpdateUserEmail_thenEmailUpdatedAndUserUpdated() {
        Long userId = 1L;

        String newEmail = "new.email@gmail.com";
        User user = new User();
        user.setId(userId);
        user.setEmail(newEmail);

        String oldEmail = "old.email@gmail.com";
        User userFromDatabase = new User();
        userFromDatabase.setId(userId);
        userFromDatabase.setEmail(oldEmail);

        User spyUpdatedUser = spy(user);
        User spyUserFromDatabase = spy(userFromDatabase);

        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> emailArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUserFromDatabase));

        userService.updateUserEmail(spyUpdatedUser);

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedId = userIdArgumentCaptor.getValue();
        assertThat(usedId).isEqualTo(userId);

        verify(spyUserFromDatabase, times(1)).setEmail(emailArgumentCaptor.capture());
        String setEmail = emailArgumentCaptor.getValue();
        assertThat(setEmail).isEqualTo(newEmail);

        verify(spyUpdatedUser, times(1)).getEmail();
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());

        User mergedUser = userArgumentCaptor.getValue();
        assertThat(mergedUser).isSameAs(spyUserFromDatabase);

        assertThat(spyUserFromDatabase.getEmail()).isEqualTo(newEmail);
    }

    @Test
    void givenUserService_whenUpdateUserEmailForUserNotInDatabase_thenResourceNotFoundExceptionIsThrown() {
        String resourceNotFoundTitle = "Brak użytkownika";
        String resourceNotFoundMessage = "Użytkownik nie istnieje";
        Long userId = 1L;

        String newEmail = "new.email@gmail.com";
        User user = new User();
        user.setId(userId);
        user.setEmail(newEmail);

        User spyUpdatedUser = spy(user);

        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(userRepository.findById(spyUpdatedUser.getId())).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> userService.updateUserEmail(spyUpdatedUser));
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        if (thrown instanceof ResourceNotFoundException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(resourceNotFoundTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(resourceNotFoundMessage)
            );
        }

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenUserService_whenChangePasswordForUserNotInDatabase_thenResourceNotFoundExceptionThrown() {
        String resourceNotFoundTitle = "Brak użytkownika";
        String resourceNotFoundMessage = "Użytkownik nie istnieje";
        Long userId = 1L;

        String password = "Password123!";
        User user = new User();
        user.setId(userId);
        user.setPassword(password);
        user.setPasswordRepeat(password);

        User spyUpdatedUser = spy(user);

        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Throwable thrown = catchThrowable(() -> userService.changePassword(spyUpdatedUser));
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        if (thrown instanceof ResourceNotFoundException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(resourceNotFoundTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(resourceNotFoundMessage)
            );
        }

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(userRepository, never()).save(any());
    }

    @Test
    void givenUserService_whenChangePassword_thenPasswordChangedAndUserUpdated() {
        Long userId = 1L;

        String newPlainPassword = "PasswordPlain";
        String newPasswordEncoded = "Password encoded";
        User user = new User();
        user.setId(userId);
        user.setPassword(newPlainPassword);
        user.setPasswordRepeat(newPlainPassword);

        String oldPassword = "OldPassword";
        User userFromDatabase = new User();
        userFromDatabase.setId(userId);
        userFromDatabase.setPassword(oldPassword);
        userFromDatabase.setPasswordRepeat(oldPassword);

        User spyUpdatedUser = spy(user);
        User spyUserFromDatabase = spy(userFromDatabase);
        PasswordResetVerificationToken passwordResetVerificationToken = new PasswordResetVerificationToken(1L, "token", LocalDateTime.now().plusMinutes(15), spyUserFromDatabase, LocalDateTime.now(), false);
        PasswordResetVerificationToken spyPasswordResetVerificationToken = spy(passwordResetVerificationToken);

        spyUserFromDatabase.setPasswordResetVerificationToken(spyPasswordResetVerificationToken);

        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> encodedPassswordArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> plainPassswordArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> isTokenConsumedArgumentCaptor = ArgumentCaptor.forClass(Boolean.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUserFromDatabase));
        when(passwordEncoder.encode(newPlainPassword)).thenReturn(newPasswordEncoded);

        userService.changePassword(spyUpdatedUser);

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(passwordEncoder).encode(plainPassswordArgumentCaptor.capture());
        String plainPasswordToEncode = plainPassswordArgumentCaptor.getValue();
        assertThat(plainPasswordToEncode).isEqualTo(newPlainPassword);

        verify(spyUserFromDatabase).setPassword(encodedPassswordArgumentCaptor.capture());
        String setEncodedPassword = encodedPassswordArgumentCaptor.getValue();
        assertThat(setEncodedPassword).isEqualTo(newPasswordEncoded);

        verify(spyUpdatedUser).getPassword();

        verify(spyUserFromDatabase).getPasswordResetVerificationToken();

        verify(spyPasswordResetVerificationToken).setConsumed(isTokenConsumedArgumentCaptor.capture());
        assertThat(isTokenConsumedArgumentCaptor.getValue()).isTrue();

        verify(userRepository).save(userArgumentCaptor.capture());
        User mergedUser = userArgumentCaptor.getValue();
        assertThat(mergedUser).isSameAs(spyUserFromDatabase);
    }

    @Test
    void givenUserService_whenFindUserByProfileId_thenResourceNotFoundExceptionIsThrown() {
        Long profileId = 1L;

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(userRepository.findByProfileId(profileId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findUserByProfileId(profileId)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Nie znaleziono takiego użytkownika");
        verify(userRepository).findByProfileId(argumentCaptor.capture());
        Long usedProductId = argumentCaptor.getValue();
        assertThat(usedProductId).isEqualTo(profileId);
    }

    @Test
    void givenUserService_whenFndUserByProfileId_thenUserIsFound() {
        Long profileId = 1L;
        Long id = 1L;
        User user = new User();
        user.setId(id);

        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);

        when(userRepository.findByProfileId(profileId)).thenReturn(Optional.of(user));

        userService.findUserByProfileId(profileId);

        verify(userRepository).findByProfileId(argumentCaptor.capture());
        Long usedProfileId = argumentCaptor.getValue();
        assertThat(usedProfileId).isEqualTo(profileId);
    }

    @Test
    void givenUserService_whenUpdateUser_thenUserUpdated() {
        User user = new User();
        user.setId(1L);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        userService.updateUser(user);

        verify(userRepository).save(userArgumentCaptor.capture());
        User userToUpdate = userArgumentCaptor.getValue();
        assertThat(userToUpdate).isEqualTo(user);
    }

    @Test
    void givenUserService_whenDeleteUserAndUserToDeleteNotInDatabase_thenResourceNotFoundExceptionThrown() {
//        Corner case 1.

        Long userIdToDelete = 1L;
        String errorTitle = "Brak użytkownika";
        String errorMessage = "Użytkownik nie istnieje";

        when(userRepository.findById(userIdToDelete)).thenReturn(Optional.empty());

        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);

        Throwable thrown = catchThrowable(() -> userService.deleteUser(userIdToDelete));
        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        if (thrown instanceof ResourceNotFoundException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(errorTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(errorMessage)
            );
        }

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userIdToDelete);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void givenUserService_whenDeleteUserThatIsOnlyAdmin_thenEntityDeletionExceptionIsThrownAndUserNotDeleted() {
//        CornerCase 2.
        String exceptionTitle = "Nie można usunąć";
        String exceptionMessage = "Jesteś jedynym administratorem. Przed usunięciem siebie nadaj innemu użytkownikowi status ADMINA";
        String adminRole = "ROLE_ADMIN";
        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));
        user.setUserTypes(spyUserTypesSet);

        User spyUser = spy(user);
        List<User> spyAdminList = spy(new ArrayList<>());

        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> userTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);

        Throwable thrown = catchThrowable(() -> userService.deleteUser(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }
        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(spyUser, times(1)).getUserTypes();
        verify(spyUserTypesSet, times(1)).stream();
        verify(spyUserType, times(1)).getRole();
        verify(spyAdminList, times(1)).isEmpty();
        assertThat(spyAdminList).isEmpty();

        verify(userRepository).findUsersByRoleNative(userTypeArgumentCaptor.capture());
        String usedUserRole = userTypeArgumentCaptor.getValue();
        assertThat(usedUserRole).isEqualTo(adminRole);

        verify(spyUser, never()).getDonations();
        verify(userRepository, never()).delete(any());

    }

    @Test
    void givenUserService_whenDeleteUserThatIsAdminAndNoOtherAdminsIsEnabledAndTheyAreNotBlocked_thenThrowEntityDeletionExceptionAndUserNotDeleted() {
//        CornerCase 3
        String exceptionTitle = "Nie można usunąć";
        String exceptionMessage = "Jesteś jedynym administratorem. Przed usunięciem siebie nadaj innemu użytkownikowi status ADMINA";
        String adminRole = "ROLE_ADMIN";
        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);

        User anotherAdminUser = new User();
        anotherAdminUser.setId(2L);
        anotherAdminUser.setUserTypes(spyUserTypesSet);
        anotherAdminUser.setEnabled(false);
        anotherAdminUser.setBlocked(false);

        User spyUser = spy(user);
        User spyAnotherUser = spy(anotherAdminUser);
        List<User> spyAdminList = spy(new ArrayList<>(List.of(spyAnotherUser)));


        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> userTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);


        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);

        Throwable thrown = catchThrowable(() -> userService.deleteUser(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(userRepository).findUsersByRoleNative(userTypeArgumentCaptor.capture());
        String usedUserType = userTypeArgumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(adminRole);

        verify(spyAdminList, times(1)).isEmpty();
        assertThat(spyAdminList).isNotEmpty();
        verify(spyAdminList, times(1)).stream();
        verify(spyAnotherUser, times(1)).isEnabled();
        verify(spyAnotherUser, never()).isBlocked();

        verify(spyUser, times(1)).getUserTypes();
        verify(spyUserType, times(1)).getRole();
        verify(userRepository, never()).delete(any());

        verify(spyUser, never()).getDonations();
        verify(userRepository, never()).delete(any());
    }

    @Test
    void givenUserService_whenDeleteUserWhichIsAdminAndOtherAdminsAreNotEnabledAndBlocked_thenEntityDeletionExceptionIsThrownAdnUserNotDeleted() {
        //        CornerCase 4
        String exceptionTitle = "Nie można usunąć";
        String exceptionMessage = "Jesteś jedynym administratorem. Przed usunięciem siebie nadaj innemu użytkownikowi status ADMINA";
        String adminRole = "ROLE_ADMIN";

        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);

        User anotherAdminUser = new User();
        anotherAdminUser.setId(2L);
        anotherAdminUser.setUserTypes(spyUserTypesSet);
        anotherAdminUser.setEnabled(false);
        anotherAdminUser.setBlocked(true);

        User spyUser = spy(user);
        User spyAnotherUser = spy(anotherAdminUser);
        List<User> spyAdminList = spy(new ArrayList<>(List.of(spyAnotherUser)));


        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> userTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);


        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);

        Throwable thrown = catchThrowable(() -> userService.deleteUser(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(userRepository).findUsersByRoleNative(userTypeArgumentCaptor.capture());
        String usedUserType = userTypeArgumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(adminRole);

        verify(spyUser, times(1)).getUserTypes();
        verify(spyUserType, times(1)).getRole();
        verify(spyAdminList, times(1)).isEmpty();
        assertThat(spyAdminList).isNotEmpty();
        verify(spyAdminList, times(1)).stream();
        verify(spyAnotherUser, times(1)).isEnabled();
        verify(spyAnotherUser, never()).isBlocked();
        verify(spyUser, never()).getDonations();
        verify(userRepository, never()).delete(any());
    }

    @Test
    void givenUserService_whenDeleteUserWhichIsAdminAndOtherAdminsEnabledButBlocked_thenEntityDeletionExceptionIsThrownAndUserNotDeleted(){
//        CornerCase 5
        String exceptionTitle = "Nie można usunąć";
        String exceptionMessage = "Jesteś jedynym administratorem. Przed usunięciem siebie nadaj innemu użytkownikowi status ADMINA";
        String adminRole = "ROLE_ADMIN";

        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);

        User anotherAdminUser = new User();
        anotherAdminUser.setId(2L);
        anotherAdminUser.setUserTypes(spyUserTypesSet);
        anotherAdminUser.setEnabled(true);
        anotherAdminUser.setBlocked(true);

        User spyUser = spy(user);
        User spyAnotherUser = spy(anotherAdminUser);
        List<User> spyAdminList = spy(new ArrayList<>(List.of(spyAnotherUser)));


        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> userTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);


        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);

        Throwable thrown = catchThrowable(() -> userService.deleteUser(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(userRepository).findUsersByRoleNative(userTypeArgumentCaptor.capture());
        String usedUserType = userTypeArgumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(adminRole);

        verify(spyUser, times(1)).getUserTypes();
        verify(spyUserType, times(1)).getRole();
        verify(spyAdminList, times(1)).isEmpty();
        verify(spyAdminList, times(2)).stream();
        verify(spyAnotherUser, times(1)).isEnabled();
        verify(spyAnotherUser, times(1)).isBlocked();
        verify(spyUser, never()).getDonations();
        verify(spyUser, times(1)).getUserTypes();
        verify(userRepository, never()).delete(any());
    }

    @Test
    void givenUserService_whenDeleteUserWhichIsAdminAndOtherAdminsEnabledAndNotBlocked_thenUserDeleted() {
        //        CornerCase 6
        String adminRole = "ROLE_ADMIN";

        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        Donation donation = new Donation();
        Donation spyDonation = spy(donation);

        user.setUserTypes(spyUserTypesSet);
        User spyUser = spy(user);
        spyDonation.setUser(spyUser);
        List<Donation> donationSpyList = spy(new ArrayList<>(List.of(spyDonation)));
        spyUser.setDonations(donationSpyList);

        User anotherAdminUser = new User();
        anotherAdminUser.setId(2L);
        anotherAdminUser.setUserTypes(spyUserTypesSet);
        anotherAdminUser.setEnabled(true);
        anotherAdminUser.setBlocked(false);


        User spyAnotherUser = spy(anotherAdminUser);
        List<User> spyAdminList = spy(new ArrayList<>(List.of(spyAnotherUser)));

        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> userTypeArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<User> userToBeRemovedArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);

        assertThatNoException().isThrownBy(() -> userService.deleteUser(userId));

        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(spyUser, times(2)).getUserTypes();
        verify(spyUserTypesSet, times(1)).stream();
        verify(spyUserType, times(1)).getRole();

        verify(userRepository).findUsersByRoleNative(userTypeArgumentCaptor.capture());
        String usedUserType = userTypeArgumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(adminRole);

        verify(spyAdminList, times(1)).isEmpty();
        assertThat(spyAdminList).isNotEmpty();
        verify(spyAdminList, times(2)).stream();
        verify(spyAnotherUser, times(1)).isEnabled();
        verify(spyAnotherUser, times(1)).isBlocked();

        verify(spyUser, times(1)).getDonations();
        verify(spyDonation, times(1)).setUser(null);
        verify(spyUserType, times(1)).removeUser(userToBeRemovedArgumentCaptor.capture());
        User deletedUserFromUserTypeUsers = userToBeRemovedArgumentCaptor.getValue();
        assertThat(deletedUserFromUserTypeUsers).isSameAs(spyUser);

        donationSpyList.forEach(donationItem -> assertThat(donationItem.getUser()).isNull());

        spyUserTypesSet.forEach(userType -> assertThat(userType.getUsers()).isEmpty());


        verify(userRepository, times(1)).delete(userArgumentCaptor.capture());
        User deletedUser = userArgumentCaptor.getValue();
        assertThat(deletedUser).isSameAs(spyUser);


    }

    @Test
    void givenUserService_whenDeleteUserWhichIsNotAdmin_thenUserDeleted() {
        //        CornerCase 7
        String adminRole = "ROLE_ADMIN";
        String userRole = "ROLE_USER";
        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        UserType spyUserType = getSpyUserType(userRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        Donation donation = new Donation();
        Donation spyDonation = spy(donation);

        user.setUserTypes(spyUserTypesSet);
        User spyUser = spy(user);
        spyDonation.setUser(spyUser);
        List<Donation> donationSpyList = spy(new ArrayList<>(List.of(spyDonation)));
        spyUser.setDonations(donationSpyList);

        ArgumentCaptor<Long> userIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<User> userToBeRemovedArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));

        assertThatNoException().isThrownBy(() -> userService.deleteUser(userId));
        verify(userRepository).findById(userIdArgumentCaptor.capture());
        Long usedUserId = userIdArgumentCaptor.getValue();
        assertThat(usedUserId).isEqualTo(userId);

        verify(spyUser, times(2)).getUserTypes();
        verify(spyUserTypesSet, times(1)).stream();
        verify(spyUserType, times(1)).getRole();
        verify(userRepository, never()).findUsersByRoleNative(adminRole);

        verify(spyUser, times(1)).getDonations();
        verify(spyDonation, times(1)).setUser(null);
        verify(spyUserType, times(1)).removeUser(userToBeRemovedArgumentCaptor.capture());
        User userDeletedFormUserTypeUserList = userToBeRemovedArgumentCaptor.getValue();
        assertThat(userDeletedFormUserTypeUserList).isSameAs(spyUser);
        verify(userRepository, times(1)).delete(userArgumentCaptor.capture());
        User deletedUser = userArgumentCaptor.getValue();
        assertThat(deletedUser).isSameAs(spyUser);
    }

    @Test
    void givenUserService_whenFindAllUsersAndListIsEmptyUserIsNotOfUserType_thenReturnEmptyList() {
        String userType = "ROLE_USER";
        User user = new User();
        user.setId(1L);
        User spyUser = spy(user);

        List<User> spyUserList = spy(new ArrayList<>());
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findUsersByRoleNative(userType)).thenReturn(spyUserList);

        List<User> returnedUsers = userService.findAllUsers(spyUser);
        assertThat(returnedUsers).isEmpty();

        verify(userRepository, times(1)).findUsersByRoleNative(argumentCaptor.capture());
        String usedRoleType = argumentCaptor.getValue();
        assertThat(usedRoleType).isEqualTo(userType);

        verify(spyUser, never()).getId();
    }

    @Test
    void givenUserService_whenFindAllUsersAndUserIsNotOfUserType_thenReturnEmptyList() {
        String userType = "ROLE_USER";
        User user = new User();
        user.setId(1L);
        User spyUser = spy(user);

        User userTwo = new User();
        userTwo.setId(2L);
        User spyUserTwo = spy(userTwo);

        User userThree = new User();
        userThree.setId(3L);
        User spyUserThree = spy(userThree);

        List<User> spyUserList = spy(new ArrayList<>(List.of(spyUserTwo, spyUserThree)));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findUsersByRoleNative(userType)).thenReturn(spyUserList);

        List<User> returnedUsers = userService.findAllUsers(spyUser);
        assertThat(returnedUsers).hasSize(2);
        assertThat(returnedUsers).doesNotContain(spyUser);

        verify(userRepository, times(1)).findUsersByRoleNative(argumentCaptor.capture());
        String usedUserType = argumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(userType);

        verify(spyUser, times(2)).getId();
    }

    @Test
    void givenUserService_whenFindAllUsersAndUserIsOfTypeUser_thenListReturnedWithoutUser() {
        String userType = "ROLE_USER";
        User user = new User();
        user.setId(1L);
        User spyUser = spy(user);

        User userTwo = new User();
        userTwo.setId(2L);
        User spyUserTwo = spy(userTwo);

        User userThree = new User();
        userThree.setId(3L);
        User spyUserThree = spy(userThree);

        List<User> spyUserList = spy(new ArrayList<>(List.of(spyUser, spyUserTwo, spyUserThree)));
        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findUsersByRoleNative(userType)).thenReturn(spyUserList);

        List<User> returnedUsers = userService.findAllUsers(spyUser);
        assertAll(
                () -> assertThat(returnedUsers).hasSize(2),
                () -> assertThat(returnedUsers).doesNotContain(spyUser)
        );

        verify(userRepository, times(1)).findUsersByRoleNative(argumentCaptor.capture());
        String usedUserType = argumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(userType);
        verify(spyUser, times(4)).getId();
    }

    @Test
    void givenUserService_whenBlockUserAndExceptionIsThrown_thenUserNotBlocked() {
//        Arrange
        User user = spy(User.class);
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

//        Act & Assert
       Throwable throwable = catchThrowable(() -> userService.blockUserById(userId));
       assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).findById(longArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(userId);

        verify(user, never()).setBlocked(any(Boolean.class));

        verify(userRepository, never()).save(any(User.class));

    }

    @Test
    void givenUserService_whenBlockUser_thenUserByIdIsBlockedAndUpdated() {
        User user = spy(User.class);
        Long userId = 1L;
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.blockUserById(userId);

        verify(user).setBlocked(argumentCaptor.capture());
        boolean isBlocked = argumentCaptor.getValue();
        assertThat(isBlocked).isTrue();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).findById(longArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(userId);

        verify(userRepository).save(userArgumentCaptor.capture());
        User userToUpdate = userArgumentCaptor.getValue();
        assertThat(userToUpdate).isEqualTo(user);
    }

    @Test
    void givenUserService_whenUnblockUser_thenUserByIdIsUnblockedAndUpdated() {
        User user = spy(User.class);
        Long userId = 1L;
        ArgumentCaptor<Boolean> argumentCaptor = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.unblockUser(userId);

        verify(user).setBlocked(argumentCaptor.capture());
        boolean isBlocked = argumentCaptor.getValue();
        assertThat(isBlocked).isFalse();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).findById(longArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(userId);

        verify(userRepository).save(userArgumentCaptor.capture());
        User userToUpdate = userArgumentCaptor.getValue();
        assertThat(userToUpdate).isEqualTo(user);
    }

    @Test
    void givenUserService_whenUnBlockUserAndExceptionIsThrown_thenUserNotBlocked() {
//        Arrange
        User user = spy(User.class);
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

//        Act & Assert
        Throwable throwable = catchThrowable(() -> userService.blockUserById(userId));
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);

        verify(user, never()).setBlocked(any(Boolean.class));

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).findById(longArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(userId);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenUserService_whenAddAdminRoleAndRoleNotFound_thenResourceNotFoundExceptionThrownAndUserNotSaved() {
        User user = spy(User.class);
        Long userTypeId = 2L;
        Long userId = 1L;

        ArgumentCaptor<Long> userTypeIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userTypeService.findById(userTypeId)).thenThrow(new ResourceNotFoundException("Test message title", "Test message"));

        assertThatThrownBy(() -> userService.addAdminRole(userId)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Test message");

        verify(userTypeService).findById(userTypeIdArgumentCaptor.capture());
        Long usedUserTypeId = userTypeIdArgumentCaptor.getValue();
        assertThat(usedUserTypeId).isEqualTo(userTypeId);

        verify(user, never()).addUserType(any());

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).findById(longArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(userId);

        verify(userRepository, never()).save(user);
    }

    @Test
    void givenUserService_whenAddAdminRoleAndNotFoundUser_thenResourceNotFoundExceptionThrownAndUserNotSaved() {
        User user = spy(User.class);
        Long userTypeId = 2L;
        Long userId = 1L;
        UserType userType = new UserType(2L, "ROLE_ADMIN", new ArrayList<>());
        String exceptionTitle = "Brak użytkownika";
        String exceptionMessage = "Użytkownik nie istnieje";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Throwable throwable = catchThrowable(() -> userService.addAdminRole(userId));
        assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
        if (throwable instanceof BusinessException e) {
            assertAll(
                    () -> assertThat(e.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(e.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(userTypeService, never()).findById(any(Long.class));

        verify(user, never()).addUserType(any());

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).findById(longArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(userId);

        verify(userRepository, never()).save(user);
    }

    @Test
    void givenUserService_whenAddAdminRoleToUser_thenRoleAdminAdded() {
        User user = spy(User.class);
        user.setUserTypes(new HashSet<>());
        Long userTypeId = 2L;
        Long userId = 1L;

        UserType userType = new UserType(2L, "ROLE_ADMIN", new ArrayList<>());

        ArgumentCaptor<Long> userTypeIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<UserType> userTypeArgumentCaptor = ArgumentCaptor.forClass(UserType.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(userTypeService.findById(userTypeId)).thenReturn(userType);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        userService.addAdminRole(userId);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository).findById(longArgumentCaptor.capture());
        Long capturedLong = longArgumentCaptor.getValue();
        assertThat(capturedLong).isEqualTo(userId);

        verify(userTypeService).findById(userTypeIdArgumentCaptor.capture());
        Long usedUserTypeId = userTypeIdArgumentCaptor.getValue();
        assertThat(usedUserTypeId).isEqualTo(userTypeId);

        verify(user).addUserType(userTypeArgumentCaptor.capture());
        UserType userTypeToAdd = userTypeArgumentCaptor.getValue();
        assertThat(userTypeToAdd).isEqualTo(userType);

        verify(userRepository).save(userArgumentCaptor.capture());
        User userToSave = userArgumentCaptor.getValue();
        assertThat(userToSave).isEqualTo(user);
    }

    @Test
    void givenUserService_whenRemoveAdminRoleForUserThatDoesNotFaveAdminRole_thenThrowEntityDeletionException() {
        String exceptionTitle = "Nie usunąć funkcji admina";
        String exceptionMessage = "Ten użytkownik nie posiada statusu admina";
        String userRole = "ROLE_USER";
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        UserType spyUserType = getSpyUserType(userRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);
        User spyUser = spy(user);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));

        Throwable thrown = catchThrowable(() -> userService.removeAdminRole(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        verify(spyUser, times(1)).getUserTypes();
        verify(spyUserTypesSet, times(1)).stream();
        verify(spyUserType, times(1)).getRole();
        verify(userRepository, never()).save(any());
        verify(userTypeService, never()).findById(any());
        verify(spyUser, never()).removeUserType(any());
    }

    @Test
    void givenUserService_whenRemoveAdminRoleAndAdminListIsEmpty_thenThrowEntityDeletionException() {
        String exceptionTitle = "Nie usunąć funkcji admina";
        String exceptionMessage = "Jesteś jedynym administratorem. Przed usunięciem funkcji nadaj innemu użytkownikowi status ADMINA";
        String adminRole = "ROLE_ADMIN";
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);
        User spyUser = spy(user);
        List<User> spyAdminUsers = spy(new ArrayList<>());

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminUsers);

        Throwable thrown = catchThrowable(() -> userService.removeAdminRole(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(spyUser, times(1)).getUserTypes();
        verify(spyUserTypesSet, times(1)).stream();
        verify(spyUserType, times(1)).getRole();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        verify(userRepository).findUsersByRoleNative(stringArgumentCaptor.capture());
        String usedUserType = stringArgumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(adminRole);

        verify(spyAdminUsers, times(1)).isEmpty();
        verify(userTypeService, never()).findById(any());
        verify(spyUser, never()).removeUserType(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void givenUserService_whenRemoveAdminRoleAdminListNotEmptyButAdminsAreNotEnabledAndBlocked_thenEntityDeletionExceptionIsThrown() {
        String exceptionTitle = "Nie usunąć funkcji admina";
        String exceptionMessage = "Jesteś jedynym administratorem. Przed usunięciem funkcji nadaj innemu użytkownikowi status ADMINA";
        String adminRole = "ROLE_ADMIN";
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);

        User anotherAdminUser = new User();
        anotherAdminUser.setId(2L);
        anotherAdminUser.setUserTypes(spyUserTypesSet);
        anotherAdminUser.setEnabled(false);
        anotherAdminUser.setBlocked(true);

        User spyUser = spy(user);
        User spyAnotherUser = spy(anotherAdminUser);
        List<User> spyAdminList = spy(new ArrayList<>(List.of(spyAnotherUser)));

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);

        Throwable thrown = catchThrowable(() -> userService.removeAdminRole(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(userRepository).findUsersByRoleNative(stringArgumentCaptor.capture());
        String usedUserType = stringArgumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(adminRole);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);


        verify(spyAdminList, times(1)).isEmpty();
        verify(spyAdminList, times(1)).stream();
        verify(spyAnotherUser, times(1)).isEnabled();
        verify(spyAnotherUser, never()).isBlocked();
        verify(userTypeService, never()).findById(any());
        verify(spyUser, never()).removeUserType(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void givenUserService_whenRemoveAdminRoleAdminListNotEmptyButAdminsNotEnabledButNotBlocked_thenEntityDeletionExceptionThrown() {
        String exceptionTitle = "Nie usunąć funkcji admina";
        String exceptionMessage = "Jesteś jedynym administratorem. Przed usunięciem funkcji nadaj innemu użytkownikowi status ADMINA";
        String adminRole = "ROLE_ADMIN";
        Long userId = 1L;
        User user = new User();
        user.setId(1L);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);

        User anotherAdminUser = new User();
        anotherAdminUser.setId(2L);
        anotherAdminUser.setUserTypes(spyUserTypesSet);
        anotherAdminUser.setEnabled(false);
        anotherAdminUser.setBlocked(false);

        User spyUser = spy(user);
        User spyAnotherUser = spy(anotherAdminUser);
        List<User> spyAdminList = spy(new ArrayList<>(List.of(spyAnotherUser)));

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);

        Throwable thrown = catchThrowable(() -> userService.removeAdminRole(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(userRepository).findUsersByRoleNative(stringArgumentCaptor.capture());
        String usedUserType = stringArgumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(adminRole);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        verify(spyAdminList, times(1)).isEmpty();
        verify(spyAdminList, times(1)).stream();
        verify(spyAnotherUser, times(1)).isEnabled();
        verify(spyAnotherUser, never()).isBlocked();
        verify(userTypeService, never()).findById(any());
        verify(spyUser, never()).removeUserType(any());
        verify(userRepository, never()).save(any());
    }

    private static UserType getSpyUserType(String adminRole) {
        return spy(new UserType(2L, adminRole, new ArrayList<>()));
    }

    @Test
    void givenUserService_whenRemoveAdminRoleAdminListNotEmptyButAdminsEnabledButBlocked_thenEntityDeletionExceptionThrown() {
        String exceptionTitle = "Nie usunąć funkcji admina";
        String exceptionMessage = "Jesteś jedynym administratorem. Przed usunięciem funkcji nadaj innemu użytkownikowi status ADMINA";
        String adminRole = "ROLE_ADMIN";
        Long userId = 1L;

        User user = new User();
        user.setId(userId);
        UserType spyUserType = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);

        User anotherAdminUser = new User();
        anotherAdminUser.setId(2L);
        anotherAdminUser.setUserTypes(spyUserTypesSet);
        anotherAdminUser.setEnabled(true);
        anotherAdminUser.setBlocked(true);

        User spyUser = spy(user);
        User spyAnotherUser = spy(anotherAdminUser);
        List<User> spyAdminList = spy(new ArrayList<>(List.of(spyAnotherUser)));

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);

        Throwable thrown = catchThrowable(() -> userService.removeAdminRole(userId));
        assertThat(thrown).isInstanceOf(EntityDeletionException.class);
        if (thrown instanceof EntityDeletionException exception) {
            assertAll(
                    () -> assertThat(exception.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(exception.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(userRepository).findUsersByRoleNative(stringArgumentCaptor.capture());
        String usedUserType = stringArgumentCaptor.getValue();
        assertThat(usedUserType).isEqualTo(adminRole);

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        verify(spyAdminList, times(1)).isEmpty();
        verify(spyAdminList, times(2)).stream();
        verify(spyAnotherUser, times(1)).isEnabled();
        verify(spyAnotherUser, times(1)).isBlocked();
        verify(userTypeService, never()).findById(any());
        verify(spyUser, never()).removeUserType(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void givenUserService_whenRemoveAdminRoleAdminListNotEmptyAdminsEnabledAndNotBlocked_thenAdminRoleRemovedAndUserUpdated() {
        String adminRole = "ROLE_ADMIN";
        Long adminRoleId = 2L;
        Long userId = 1L;
        User user = new User();
        user.setId(userId);
        UserType spyUserType = getSpyUserType(adminRole);
        UserType spyUserTypeFromDatabase = getSpyUserType(adminRole);
        Set<UserType> spyUserTypesSet = spy(new HashSet<>(Set.of(spyUserType)));

        user.setUserTypes(spyUserTypesSet);

        User anotherAdminUser = new User();
        anotherAdminUser.setId(2L);
        anotherAdminUser.setUserTypes(spyUserTypesSet);
        anotherAdminUser.setEnabled(true);
        anotherAdminUser.setBlocked(false);

        User spyUser = spy(user);
        User spyAnotherUser = spy(anotherAdminUser);
        List<User> spyAdminList = spy(new ArrayList<>(List.of(spyAnotherUser)));

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> userRoleIdArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<UserType> userTypeArgumentCaptor = ArgumentCaptor.forClass(UserType.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        when(userRepository.findById(userId)).thenReturn(Optional.of(spyUser));
        when(userRepository.findUsersByRoleNative(adminRole)).thenReturn(spyAdminList);
        when(userTypeService.findById(adminRoleId)).thenReturn(spyUserTypeFromDatabase);

        assertThatNoException().isThrownBy(() -> userService.removeAdminRole(userId));
        verify(spyUser, times(2)).getUserTypes();
        verify(spyUserTypesSet, times(1)).stream();
        verify(spyUserType, times(1)).getRole();

        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isSameAs(userId);

        verify(userRepository).findUsersByRoleNative(stringArgumentCaptor.capture());
        String usedAdminRole = stringArgumentCaptor.getValue();
        assertThat(usedAdminRole).isEqualTo(adminRole);

        verify(spyAdminList, times(1)).isEmpty();
        verify(spyAdminList, times(2)).stream();
        verify(spyAnotherUser, times(1)).isEnabled();
        verify(spyAnotherUser, times(1)).isBlocked();
        verify(userTypeService, times(1)).findById(userRoleIdArgumentCaptor.capture());
        Long usedUserRoleId = userRoleIdArgumentCaptor.getValue();
        assertThat(usedUserRoleId).isEqualTo(adminRoleId);
        verify(spyUser, times(1)).removeUserType(userTypeArgumentCaptor.capture());
        UserType removedUserType = userTypeArgumentCaptor.getValue();
        assertThat(removedUserType).isSameAs(spyUserTypeFromDatabase);
        verify(userRepository).save(userArgumentCaptor.capture());
        User mergedUser = userArgumentCaptor.getValue();
        assertThat(mergedUser).isSameAs(spyUser);
    }

    @Test
    void givenUserService_whenResetPasswordForNotFoundUser_thenUsernameNotFoundExceptionThrown() {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        Email email = new Email("email@gmail.com");

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        when(userRepository.findByEmail(email.getAddressEmail())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword(email, servletRequest)).isInstanceOf(UsernameNotFoundException.class).hasMessage("There is no such user");
        verify(userRepository).findByEmail(argumentCaptor.capture());
        String usedEmail = argumentCaptor.getValue();

        assertThat(usedEmail).isEqualTo(email.getAddressEmail());
    }

    @Test
    void givenUserService_whenResetPassword_thenPasswordResetEventPublished() {
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        Email email = new Email("email@gmail.com");
        String expectedUrl = "http://localhost:8000/app";

        Long id = 1L;
        User user = new User();
        user.setId(id);
        user.setEmail(email.getAddressEmail());

        ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PasswordResetEvent> passwordResetEventArgumentCaptor = ArgumentCaptor.forClass(PasswordResetEvent.class);

        when(servletRequest.getServerName()).thenReturn("localhost");
        when(servletRequest.getServerPort()).thenReturn(8000);
        when(servletRequest.getContextPath()).thenReturn("/app");
        when(userRepository.findByEmail(email.getAddressEmail())).thenReturn(Optional.of(user));

        userService.resetPassword(email, servletRequest);

        verify(userRepository, times(1)).findByEmail(argumentCaptor.capture());
        String usedEmail = argumentCaptor.getValue();
        assertThat(usedEmail).isEqualTo(email.getAddressEmail());

        verify(publisher, times(1)).publishEvent(passwordResetEventArgumentCaptor.capture());
        PasswordResetEvent event = passwordResetEventArgumentCaptor.getValue();

        verify(servletRequest, times(1)).getServerName();
        verify(servletRequest, times(1)).getServerPort();
        verify(servletRequest, times(1)).getContextPath();

        assertAll(
                () -> assertThat(event.getApplicationUrl()).isEqualTo(expectedUrl),
                () -> assertThat(event.getUser()).isEqualTo(user)
        );
    }

    @Test
    void whenChangeEmail_thenEmailChangedAndUseSaved() {
//        Arrange
        User user = new User();
        Long userId = 1L;
        String testEmail = "test@email.com";
        user.setEmail(testEmail);
        user.setId(userId);

        User spyUser = spy(user);
        User userFromDatabaseSpy = spy(new User());

        when(userRepository.findById(userId)).thenReturn(Optional.of(userFromDatabaseSpy));

//        Act
        userService.changeEmail(spyUser);

        verify(spyUser, times(1)).getId();
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(spyUser.getId());

        verify(spyUser, times(1)).getEmail();
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(userFromDatabaseSpy, times(1)).setEmail(stringArgumentCaptor.capture());
        String capturedEmail = stringArgumentCaptor.getValue();
        assertThat(capturedEmail).isEqualTo(testEmail);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertThat(capturedUser).isSameAs(userFromDatabaseSpy);
    }
}