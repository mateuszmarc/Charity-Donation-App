package pl.mateuszmarcyk.charity_donation_app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.exception.BusinessException;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.repository.UserTypeRepository;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserInitializerTest {

    @InjectMocks
    private AdminUserInitializer adminUserInitializer;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserTypeRepository userTypeRepository;

    @Mock
    private PasswordEncoder passwordEncoder;


    @Test
    void whenRun_thenThrowResourceNotFoundException() {
        String exceptionTitle = "Cannot find";
        String exceptionMessage = "Cannot find user type";
        when(userTypeRepository.findById(2L)).thenReturn(Optional.empty());
        Throwable thrown = catchThrowable(() -> adminUserInitializer.run());

        assertThat(thrown).isInstanceOf(ResourceNotFoundException.class);
        if (thrown instanceof BusinessException e) {
            assertAll(
                    () -> assertThat(e.getTitle()).isEqualTo(exceptionTitle),
                    () -> assertThat(e.getMessage()).isEqualTo(exceptionMessage)
            );
        }

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void givenNoAdminUserInDatabase_whenRun_thenNewAdminUserCreated() {
//        Arrange
        String adminEmail = "admin@admin.com";
        String plainPassword = "Admin123!";
        String encodedPassword = "Encoded123!";
        UserType userType = spy(new UserType(2L, "ROLE_ADMIN", new ArrayList<>()));

        when(userTypeRepository.findById(2L)).thenReturn(Optional.of(userType));

        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(plainPassword)).thenReturn(encodedPassword);

        //        Act
        adminUserInitializer.run();

//        Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userTypeRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(2L);

        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(passwordEncoder, times(1)).encode(stringArgumentCaptor.capture());
        String capturedPlainPassword = stringArgumentCaptor.getValue();
        assertThat(capturedPlainPassword).isEqualTo(plainPassword);

        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userArgumentCaptor.capture());
        User capturedUser = userArgumentCaptor.getValue();
        assertAll(
                () -> assertThat(capturedUser.getEmail()).isEqualTo(adminEmail),
                () -> assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword),
                () -> assertThat(capturedUser.isEnabled()).isTrue(),
                () -> assertThat(capturedUser.isBlocked()).isFalse(),
                () -> assertThat(capturedUser.getUserTypes()).contains(userType),
                () -> assertThat(capturedUser.getProfile()).isNotNull(),
                () -> assertThat(capturedUser.getVerificationToken()).isNotNull(),
                () -> assertThat(capturedUser.getPasswordResetVerificationToken()).isNull()
        );
    }

    @Test
    void givenAdminUserInDatabase_whenRun_ThenNoUserCreated() {
//        Arrange
        String adminEmail = "admin@admin.com";
        UserType userType = spy(new UserType(2L, "ROLE_ADMIN", new ArrayList<>()));
        User user = new User();
        when(userTypeRepository.findById(2L)).thenReturn(Optional.of(userType));
        when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(user));

//       Act
        adminUserInitializer.run();

//        Assert
        ArgumentCaptor<Long> longArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(userTypeRepository, times(1)).findById(longArgumentCaptor.capture());
        Long capturedId = longArgumentCaptor.getValue();
        assertThat(capturedId).isEqualTo(2L);

        verify(passwordEncoder, never()).encode(any(String.class));

        verify(userRepository, never()).save(any(User.class));
    }
}