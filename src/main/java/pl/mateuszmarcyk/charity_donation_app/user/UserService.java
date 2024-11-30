package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.EntityDeletionException;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyConsumedException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyExpiredException;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserType;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserTypeService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserTypeService userTypeService;
    private final VerificationTokenService verificationTokenService;
    private final Long USER_ROLE_ID = 1L;

    @Value("$[error.tokennotfound.title}")
    private String tokenErrorTitle;

    @Value("${error.tokenconsumed.message}")
    private String tokenConsumedMessage;

    @Value("${error.tokenexpired.message}")
    private String tokenExpiredMessage;

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
    public void updateUserEmail(@Valid User userToEdit) {

        User userInDatabase = findUserById(userToEdit.getId());

        userInDatabase.setEmail(userToEdit.getEmail());
        
        userRepository.save(userInDatabase);
    }

    public void changePassword(@Valid User user) {

        User userFromDatabase = findUserById(user.getId());

        System.out.println("Password to be saved: " + user.getPassword());
        userFromDatabase.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(userFromDatabase);
    }

    public User findUserByProfileId(Long id) {

        return userRepository.findByProfileId(id).orElseThrow(() -> new ResourceNotFoundException("Brak użytkownika", "Nie znaleziono takiego użytkownika"));
    }

    public void updateUser(User profileOwner) {
        userRepository.save(profileOwner);
    }

    public void removeAuthority(User userToRemoveAuthorityFrom, String roleAdmin) {
        userToRemoveAuthorityFrom.getUserTypes().removeIf(userType -> userType.getRole().equals(roleAdmin));
        userRepository.save(userToRemoveAuthorityFrom);
    }


    public void deleteAdmin(User userToDelete, User loggedUser) {

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

    public void blockUser(User userToBlock) {
        userToBlock.setBlocked(true);
        userRepository.save(userToBlock);
    }

    public void unblockUser(User userToUnblock) {
        userToUnblock.setBlocked(false);
        userRepository.save(userToUnblock);
    }
}
