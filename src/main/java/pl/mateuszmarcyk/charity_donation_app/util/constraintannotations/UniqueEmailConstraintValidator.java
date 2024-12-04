package pl.mateuszmarcyk.charity_donation_app.util.constraintannotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;

import java.util.Optional;

@NoArgsConstructor
public class UniqueEmailConstraintValidator implements ConstraintValidator<UniqueEmail, User> {


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
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {
        if (user == null || user.getEmail() == null) {
            return true;
        }

        System.out.println("user id " + user.getId());


        Optional<User> optionalUser = userService.findByEmail(user.getEmail());
        if (optionalUser.isPresent()) {
            User foundUser = optionalUser.get();
            System.out.println("Found user id " + foundUser.getId());
            if (user.getId() == null) {
                return false;
            } else return user.getId().equals(foundUser.getId());

        }
        return true;
    }

}
