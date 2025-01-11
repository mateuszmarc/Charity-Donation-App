package pl.mateuszmarcyk.charity_donation_app.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.entity.*;
import pl.mateuszmarcyk.charity_donation_app.exception.EntityDeletionException;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyConsumedException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyExpiredException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.Email;
import pl.mateuszmarcyk.charity_donation_app.util.event.PasswordResetEvent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor
@Service
public class UserService {

    public static final long ADMIN_USER_TYPE_ID = 2L;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserTypeService userTypeService;
    private final VerificationTokenService verificationTokenService;
    private final Long USER_ROLE_ID = 1L;
    private final ApplicationEventPublisher publisher;
    private final PasswordResetVerificationTokenService passwordResetVerificationTokenService;
    private final MessageSource messageSource;

    @Transactional
    public User save(User user) {

        String plainPassword = user.getPassword();
        String encryptedPassword = passwordEncoder.encode(plainPassword);
        user.setPassword(encryptedPassword);

        UserType userRoleType = userTypeService.findById(USER_ROLE_ID);

        user.addUserType(userRoleType);
        user.setUserProfile(new UserProfile());
        return userRepository.save(user);
    }

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("There is no such user"));
    }

    @Transactional
    public void validateToken(String token) {
        VerificationToken verificationToken = verificationTokenService.findByToken(token);
        String tokenConsumedMessage = messageSource.getMessage("error.tokenconsumed.message", null, Locale.getDefault());
        String tokenErrorTitle = messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault());
        String tokenExpiredMessage = messageSource.getMessage("error.tokenexpired.message", null, Locale.getDefault());

        if (verificationToken.getUser().isEnabled()) {
            throw new TokenAlreadyConsumedException(tokenErrorTitle, tokenConsumedMessage);
        }

        LocalDateTime expirationTime = verificationToken.getExpirationTime();
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (expirationTime.isBefore(currentDateTime)) {
            throw new TokenAlreadyExpiredException(tokenExpiredMessage, tokenErrorTitle, token);
        }

        User user = verificationToken.getUser();

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public User validatePasswordResetToken(String token) {
        PasswordResetVerificationToken passwordResetVerificationToken = passwordResetVerificationTokenService.findByToken(token);
        String tokenConsumedMessage = messageSource.getMessage("error.tokenconsumed.message", null, Locale.getDefault());
        String tokenErrorTitle = messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault());
        String tokenExpiredMessage = messageSource.getMessage("error.tokenexpired.message", null, Locale.getDefault());


        if (passwordResetVerificationToken.isConsumed()) {
            throw new TokenAlreadyConsumedException(tokenErrorTitle, tokenConsumedMessage);
        }

        LocalDateTime expirationTime = passwordResetVerificationToken.getExpirationTime();
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (expirationTime.isBefore(currentDateTime)) {
            throw new TokenAlreadyExpiredException(tokenExpiredMessage, tokenErrorTitle, token);
        }

        return passwordResetVerificationToken.getUser();
    }

    public User findUserByVerificationToken(String token) {
        return userRepository.findUserByVerificationToken_Token(token).orElseThrow(() -> new ResourceNotFoundException("Brak użytkownika", "Użytkownik nie istnieje"));
    }

    public List<User> findAllAdmins(User user) {
        List<User> users = userRepository.findUsersByRoleNative("ROLE_ADMIN");
        users.removeIf(user1 -> user1.getId().equals(user.getId()));
        return users;
    }

    public User findUserById(Long id) {
       return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Brak użytkownika", "Użytkownik nie istnieje"));
    }

    @Transactional
    public void updateUserEmail(@Valid User userToEdit) {

        User userInDatabase = findUserById(userToEdit.getId());

        userInDatabase.setEmail(userToEdit.getEmail());

        userRepository.save(userInDatabase);
    }

    @Transactional
    public void changePassword(@Valid User user) {

        User userFromDatabase = findUserById(user.getId());

        PasswordResetVerificationToken passwordResetVerificationToken = userFromDatabase.getPasswordResetVerificationToken();
        if (passwordResetVerificationToken != null) {
            passwordResetVerificationToken.setConsumed(true);
        }
        userFromDatabase.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(userFromDatabase);
    }

    public User findUserByProfileId(Long id) {

        return userRepository.findByProfileId(id).orElseThrow(() -> new ResourceNotFoundException("Brak użytkownika", "Nie znaleziono takiego użytkownika"));
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userIdToDelete) {

        User userToDelete = findUserById(userIdToDelete);

        if (userToDelete.getUserTypes().stream().anyMatch(role -> role.getRole().equals("ROLE_ADMIN"))) {
            List<User> allAdmins = findAllAdmins(userToDelete);
            if (allAdmins.isEmpty() || allAdmins.stream().noneMatch(User::isEnabled) || allAdmins.stream().allMatch(User::isBlocked)) {
                throw new EntityDeletionException("Nie można usunąć", "Jesteś jedynym administratorem. Przed usunięciem siebie nadaj innemu użytkownikowi status ADMINA");
            }
        }

        userToDelete.getDonations().forEach(donation -> donation.setUser(null));
        userToDelete.getUserTypes().forEach(userType -> userType.removeUser(userToDelete));

        userRepository.delete(userToDelete);
    }

    public List<User> findAllUsers(User user) {
        List<User> users = userRepository.findUsersByRoleNative("ROLE_USER");
        users.removeIf(user1 -> user1.getId().equals(user.getId()));
        return users;
    }

    @Transactional
    public void blockUserById(Long userId) {
        User userToBlock = findUserById(userId);

        userToBlock.setBlocked(true);
        userRepository.save(userToBlock);
    }

    @Transactional
    public void unblockUser(Long userId) {

        User userToUnblock = findUserById(userId);

        userToUnblock.setBlocked(false);
        userRepository.save(userToUnblock);
    }

    @Transactional
    public void addAdminRole(Long userId) {
        User userToUpgrade = findUserById(userId);
        UserType userType = userTypeService.findById(ADMIN_USER_TYPE_ID);
        userToUpgrade.addUserType(userType);
        userRepository.save(userToUpgrade);
    }

    @Transactional
    public void removeAdminRole(Long userId) {

        User userToDowngrade = findUserById(userId);

        if (userToDowngrade.getUserTypes().stream().anyMatch(role -> role.getRole().equals("ROLE_ADMIN"))) {
            List<User> allAdmins = findAllAdmins(userToDowngrade);

            if (allAdmins.isEmpty() || allAdmins.stream().noneMatch(User::isEnabled) || allAdmins.stream().allMatch(User::isBlocked)) {
                throw new EntityDeletionException("Nie usunąć funkcji admina", "Jesteś jedynym administratorem. Przed usunięciem funkcji nadaj innemu użytkownikowi status ADMINA");
            }

            UserType userType = userTypeService.findById(ADMIN_USER_TYPE_ID);
            userToDowngrade.removeUserType(userType);
            userRepository.save(userToDowngrade);
        } else {
            throw new EntityDeletionException("Nie usunąć funkcji admina", "Ten użytkownik nie posiada statusu admina");
        }

    }

    @Transactional
    public void resetPassword(@Valid Email email, HttpServletRequest request) {

        User user = findUserByEmail(email.getAddressEmail());

        publisher.publishEvent(new PasswordResetEvent(user, getApplicationUrl(request)));
    }

    private String getApplicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    @Transactional
    public void changeEmail(@Valid User userToEdit) {
        User userFromDatabase = findUserById(userToEdit.getId());
        userFromDatabase.setEmail(userToEdit.getEmail());
        userRepository.save(userFromDatabase);
    }
}
