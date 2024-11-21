package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyConsumedException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyExpiredException;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.registration.verificationtoken.VerificationTokenService;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserType;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserTypeService;

import java.time.LocalDateTime;
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

        System.out.println("UserType role: " + userRoleType.getRole());

        user.grantAuthority(userRoleType);
        user.setUserProfile(new UserProfile());
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
       return userRepository.findByEmail(email);
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
}
