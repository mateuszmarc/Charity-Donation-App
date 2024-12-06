package pl.mateuszmarcyk.charity_donation_app.util.constraintannotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

public class PasswordEqualConstraintValidator implements ConstraintValidator<PasswordEqual, User> {

    @Override
    public boolean isValid(User user, ConstraintValidatorContext constraintValidatorContext) {

        System.out.println(user.getPassword());
        System.out.println(user.getPasswordRepeat());
        if (user.getPassword() == null && user.getPasswordRepeat() == null) {
            return true;
        }

       if (user.getPassword() != null && user.getPasswordRepeat() != null) {
           return user.getPassword().equals(user.getPasswordRepeat());
       }
       return false;
    }


}
