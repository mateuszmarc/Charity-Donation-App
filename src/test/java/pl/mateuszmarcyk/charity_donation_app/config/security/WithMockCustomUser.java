package pl.mateuszmarcyk.charity_donation_app.config.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithUserDetailsSecurityContextFactory.class)
public @interface WithMockCustomUser {
    String email() default "defaultUser";
    String[] roles() default { "ROLE_USER" };
    String enabled() default "true";
    String blocked() default "false";

}
