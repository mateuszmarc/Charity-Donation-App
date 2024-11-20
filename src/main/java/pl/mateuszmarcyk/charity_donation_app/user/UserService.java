package pl.mateuszmarcyk.charity_donation_app.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.userprofile.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserType;
import pl.mateuszmarcyk.charity_donation_app.usertype.UserTypeService;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    //    private final PasswordEncoder passwordEncoder;
    private final UserTypeService userTypeService;

    private final Long USER_ROLE_ID = 1L;

    @Transactional
    public User save(User user) {

        String plainPassword = user.getPassword();
        System.out.println("Password from userService: " + plainPassword);
//        String encryptedPassword = passwordEncoder.encode(plainPassword);
//        user.setPassword(encryptedPassword);
        user.setPassword(plainPassword);
        UserType userRoleType = userTypeService.findById(USER_ROLE_ID);

        System.out.println("UserType role: " + userRoleType.getRole());

        user.grantAuthority(userRoleType);
        user.setUserProfile(new UserProfile());
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
       return userRepository.findByEmail(email);
    }
}
