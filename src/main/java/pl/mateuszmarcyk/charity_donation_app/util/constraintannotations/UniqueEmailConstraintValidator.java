package pl.mateuszmarcyk.charity_donation_app.util.constraintannotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;

import java.util.Optional;

@Slf4j
@Component
@NoArgsConstructor
public class UniqueEmailConstraintValidator implements ConstraintValidator<UniqueEmail, User> {

    private UserRepository userRepository;

    @Autowired
    public UniqueEmailConstraintValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(User userToValidate, ConstraintValidatorContext constraintValidatorContext) {
        if (userToValidate.getEmail() == null) {
            return true;
        }

        log.info("Validated user id {}", userToValidate.getId());
        log.info("Validated user email  ={}", userToValidate.getEmail());


        Optional<User> optionalUser = userRepository.findByEmail(userToValidate.getEmail());
        if (optionalUser.isPresent()) {
            User foundUser = optionalUser.get();
            log.info("Found user id {}", foundUser.getId());
            log.info("Found user email {}", foundUser.getEmail());
            if (userToValidate.getId() == null) {
               log.info("Email is invalid");
                return false;
            } else {
                if (userToValidate.getId().equals(foundUser.getId())) {
                   log.info("User is updating his own email");
                    return true;
                } else {
                   log.info("They are different users - one user is in database with such email already.");
                    return false;
                }
            }
        }
        return true;
    }
}


