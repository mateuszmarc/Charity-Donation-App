package pl.mateuszmarcyk.charity_donation_app.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.util.event.RegistrationCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.util.event.ResendTokenEvent;
import pl.mateuszmarcyk.charity_donation_app.entity.User;

import java.util.Locale;

@RequiredArgsConstructor
@Service
public class RegistrationService {

    private final UserService userService;
    private final ApplicationEventPublisher publisher;
    private final MessageSource messageSource;

    public String getApplicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

    @Transactional
    public void registerUser(User user, HttpServletRequest request) {

        User savedUser = userService.save(user);

        publisher.publishEvent(new RegistrationCompleteEvent(savedUser, getApplicationUrl(request)));
    }

    public void resendToken(String oldToken, HttpServletRequest request) {

        User user = userService.findUserByVerificationToken(oldToken);
        String applicationUrl = getApplicationUrl(request);

        publisher.publishEvent(new ResendTokenEvent(user, applicationUrl, user.getVerificationToken()));
    }

    public String getRegistrationCompleteMessage() {

        String tokenValidationTimeMessage = messageSource.getMessage("token.validation.time.message", null, Locale.getDefault());
        String tokenValidTime = messageSource.getMessage("token.valid.time", null, Locale.getDefault());

        String message = tokenValidationTimeMessage + " " + tokenValidTime + " minut";
        return message;
    }
}
