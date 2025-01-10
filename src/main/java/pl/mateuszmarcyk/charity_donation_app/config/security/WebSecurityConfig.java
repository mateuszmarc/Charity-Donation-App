package pl.mateuszmarcyk.charity_donation_app.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final AuthenticationFailureHandler authenticationFailureHandler;


    private final String[] publicUrls = {
            "/",
            "/message",
            "/css/**",
            "/images/**",
            "/js/**",
            "/*.css",
            "/*.js",
            "/*.js.map",
            "/resources/**",
    };

    private final String[] urlsForUnauthenticatedOnly = {
            "/login",
            "/reset-password",
            "/register",
            "/register/**",
            "/reset-password/**",
            "/new-password",
            "/resendToken",
            "/login/**",
    };

    private final String[] userUrls = {
            "/donations/**",
            "/donate"
    };

    private final String[] availableForAuthenticated = {
            "/profile/**",
            "/account/**",
            "/error/403",
            "/error/**",
    };

    private final String[] adminUrls = {
            "/admins/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity security, AuthenticationSuccessHandler authenticationSuccessHandler) throws Exception {

        security.authenticationProvider(authenticationProvider());

        security.authorizeHttpRequests(auth -> {
            auth.requestMatchers(urlsForUnauthenticatedOnly).anonymous();
            auth.requestMatchers(publicUrls).permitAll();
            auth.requestMatchers(userUrls).hasAuthority("ROLE_USER");
            auth.requestMatchers(availableForAuthenticated).hasAnyAuthority("ROLE_USER", "ROLE_ADMIN");
            auth.requestMatchers(adminUrls).hasAuthority("ROLE_ADMIN");
            auth.anyRequest().authenticated();
        });

        security.formLogin(form ->
                form.loginPage("/login")
                        .failureHandler(authenticationFailureHandler)
                        .successHandler(customAuthenticationSuccessHandler)
                )
                .logout(logout -> {
                        logout.logoutUrl("/logout");
                        logout.logoutSuccessUrl("/");
                        logout.permitAll();
                })
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.accessDeniedHandler(customAccessDeniedHandler)
                )
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable);

        return security.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(customUserDetailsService);
        return authenticationProvider;
    }


}
