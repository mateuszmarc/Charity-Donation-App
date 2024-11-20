package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;

import java.util.Optional;

@NoArgsConstructor
public class UniqueEmailConstraintValidator implements ConstraintValidator<UniqueEmail, String> {


    private UserService userService;

    @Autowired
    public UniqueEmailConstraintValidator(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext constraintValidatorContext) {
        if (email != null) {
            return !checkForDuplicates(email);
        }
        return false;
    }

    private boolean checkForDuplicates(String email) {
        Optional<User> optionalUser = userService.findByEmail(email);
        return optionalUser.isPresent();
    }
}
