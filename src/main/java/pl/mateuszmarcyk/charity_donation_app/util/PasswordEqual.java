package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = PasswordEqualConstraintValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordEqual {

    public String message() default "Hasła różnią się od siebie";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};
}
