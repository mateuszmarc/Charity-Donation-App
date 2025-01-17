package pl.mateuszmarcyk.charity_donation_app.util.constraintannotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

@Component
public class PasswordEqualConstraintValidator implements ConstraintValidator<PasswordEqual, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {

        if (user.getPassword() == null && user.getPasswordRepeat() == null) {
            return true;
        }

       if (user.getPassword() != null && user.getPasswordRepeat() != null) {
           return user.getPassword().equals(user.getPasswordRepeat());
       }
       return false;
    }


}
