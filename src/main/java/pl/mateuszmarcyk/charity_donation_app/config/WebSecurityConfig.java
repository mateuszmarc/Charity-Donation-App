package pl.mateuszmarcyk.charity_donation_app.config;

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
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomAuthenticationSuccessHandler;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetailsService;

@RequiredArgsConstructor
@Configuration
public class WebSecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    private final String[] publicUrls = {
            "/",
            "/app",
            "/app/message",
            "/message",
            "/app/reset-password",
            "/reset-password",
            "/register",
            "/register/**",
            "/css/**",
            "/images/**",
            "/js/**",
            "/*.css",
            "/*.js",
            "/*.js.map",
            "/resources/**"
    };

    private final String[] adminUrls = {
            "/admin/**"
    };

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity security)  throws Exception {

        security.authenticationProvider(authenticationProvider());

        security.authorizeHttpRequests(auth -> {
            auth.requestMatchers(publicUrls).permitAll();
            auth.requestMatchers(
                    "/donate",
                    "/my-donations",
                    "/profile/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN");
            auth.requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN");
            auth.anyRequest().authenticated();
        });

        security.formLogin(form ->
                form.loginPage("/login").permitAll()
                        .successHandler(customAuthenticationSuccessHandler))
                .logout(logout -> {
                        logout.logoutUrl("/logout");
                        logout.logoutSuccessUrl("/");
                })
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
