package pl.mateuszmarcyk.charity_donation_app.util.constraintannotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mateuszmarcyk.charity_donation_app.repository.UserRepository;

@NoArgsConstructor
public class UserEmailConstraintValidator implements ConstraintValidator<UserEmail, String> {

    private UserRepository userRepository;

    @Autowired
    public UserEmailConstraintValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {

        if (email == null) {
            return true;
        }
       return userRepository.findByEmail(email).isPresent();
    }
}
