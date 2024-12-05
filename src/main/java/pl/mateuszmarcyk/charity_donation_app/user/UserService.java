package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.event.PasswordResetEvent;
import pl.mateuszmarcyk.charity_donation_app.exception.EntityDeletionException;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyConsumedException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyExpiredException;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.PasswordResetVerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.PasswordResetVerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserType;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserTypeService;
import pl.mateuszmarcyk.charity_donation_app.util.constraintannotations.Email;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

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

        user.grantAuthority(userRoleType);
        user.setUserProfile(new UserProfile());
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
       return userRepository.findByEmail(email);
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

        User user = verificationToken.getUser();
        LocalDateTime expirationTime = verificationToken.getExpirationTime();
        LocalDateTime currentDateTime = LocalDateTime.now();

        if (expirationTime.isBefore(currentDateTime)) {
            throw new TokenAlreadyExpiredException(tokenErrorTitle, tokenExpiredMessage, token);
        }

        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    public User validatePasswordResetToken(String token) {
        PasswordResetVerificationToken passwordResetVerificationToken = passwordResetVerificationTokenService.findByToken(token);
        LocalDateTime expirationTime = passwordResetVerificationToken.getExpirationTime();
        LocalDateTime currentDateTime = LocalDateTime.now();
        String tokenErrorTitle = messageSource.getMessage("error.tokennotfound.title", null, Locale.getDefault());
        String tokenExpiredMessage = messageSource.getMessage("error.tokenexpired.message", null, Locale.getDefault());

        if (expirationTime.isBefore(currentDateTime)) {
            throw new TokenAlreadyExpiredException(tokenErrorTitle, tokenExpiredMessage, token);
        }

        return findByPasswordVerificationToken(token);
    }

    private User findByPasswordVerificationToken(String token) {
        return userRepository.findUserByPasswordResetVerificationToken(token).orElseThrow(() -> new ResourceNotFoundException("Użytkownik nie istnieje", "Nie ma takiego użytkownika"));
    }

    public User findByVerificationToken(String token) {
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
    public User updateUserEmail(@Valid User userToEdit) {

        User userInDatabase = findUserById(userToEdit.getId());

        userInDatabase.setEmail(userToEdit.getEmail());
        
       return userRepository.save(userInDatabase);
    }

    @Transactional
    public void changePassword(@Valid User user) {

        User userFromDatabase = findUserById(user.getId());

        System.out.println("Password to be saved: " + user.getPassword());
        userFromDatabase.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(userFromDatabase);
    }

    public User findUserByProfileId(Long id) {

        return userRepository.findByProfileId(id).orElseThrow(() -> new ResourceNotFoundException("Brak użytkownika", "Nie znaleziono takiego użytkownika"));
    }

    @Transactional
    public void updateUser(User profileOwner) {
        userRepository.save(profileOwner);
    }

    @Transactional
    public void deleteUser(Long userIdToDelete, User loggedUser) {

        User userToDelete = findUserById(userIdToDelete);
        List<User> allAdmins = findAllAdmins(loggedUser);

        if (allAdmins.isEmpty() && userToDelete.getId().equals(loggedUser.getId())) {
            throw new EntityDeletionException("Nie można usunąć", "Jesteś jedynym administratorem. Przed usunięciem siebie nadaj innemu użytkownikowi status ADMINA");
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
    public void blockUser(User userToBlock) {
        userToBlock.setBlocked(true);
        userRepository.save(userToBlock);
    }

    @Transactional
    public void unblockUser(User userToUnblock) {
        userToUnblock.setBlocked(false);
        userRepository.save(userToUnblock);
    }

    @Transactional
    public void addAdminRole(User userToUpgrade) {
        UserType userType = userTypeService.findById(2L);
        userToUpgrade.addUserType(userType);
        userRepository.save(userToUpgrade);
    }

    @Transactional
    public void removeAdminRole(User userToDowngrade, User loggedUser) {
        UserType userType = userTypeService.findById(2L);

        List<User> allAdmins = findAllAdmins(loggedUser);

        if (allAdmins.isEmpty() && userToDowngrade.getId().equals(loggedUser.getId())) {
            throw new EntityDeletionException("Nie można usunąć", "Jesteś jedynym administratorem. Przed usunięciem siebie nadaj innemu użytkownikowi status ADMINA");
        }

        userToDowngrade.removeUserType(userType);
        userRepository.save(userToDowngrade);
    }

    @Transactional
    public void resetPassword(@Valid Email email, HttpServletRequest request) {

        User user = findUserByEmail(email.getAddressEmail());

        publisher.publishEvent(new PasswordResetEvent(user, getApplicationUrl(request)));
    }

    private String getApplicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}
