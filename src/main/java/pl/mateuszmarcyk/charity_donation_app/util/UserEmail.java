package pl.mateuszmarcyk.charity_donation_app.util;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UserEmailConstraintValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserEmail {

    public String message() default "Ten email nie jest przypisany do żadnego użytkownika";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};
}
