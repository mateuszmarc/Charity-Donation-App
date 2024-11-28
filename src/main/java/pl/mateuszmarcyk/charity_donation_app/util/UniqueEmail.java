package pl.mateuszmarcyk.charity_donation_app.util;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueEmailConstraintValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueEmail {

    public String message() default "Ten email jest już w użyciu";

    public Class<?>[] groups() default {};

    public Class<? extends Payload>[] payload() default {};
}
