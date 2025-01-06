package pl.mateuszmarcyk.charity_donation_app;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.entity.UserProfile;
import pl.mateuszmarcyk.charity_donation_app.entity.UserType;
import pl.mateuszmarcyk.charity_donation_app.entity.VerificationToken;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;
import pl.mateuszmarcyk.charity_donation_app.repository.UserTypeRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class AdminUserInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserTypeRepository userTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {

        UserType adminRole = userTypeRepository.findById(2L).orElseThrow(() -> new ResourceNotFoundException("Cannot find", "Cannot find user type"));

        if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
            User admin = new User();
            admin.setEmail("admin@admin.com");
            admin.setPassword(passwordEncoder.encode("Admin123!"));
            admin.setEnabled(true);
            admin.setBlocked(false);
            admin.setRegistrationDate(LocalDateTime.now());
            admin.setUserTypes(new HashSet<>(Set.of(adminRole)));
            admin.setProfile(new UserProfile());
            VerificationToken token = new VerificationToken(UUID.randomUUID().toString(), null, 15);
            admin.setVerificationToken(token);
            userRepository.save(admin);

            System.out.println("Admin user created: admin@admin.com");
        }
    }


}
