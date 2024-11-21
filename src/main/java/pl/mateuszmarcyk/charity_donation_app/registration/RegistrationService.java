package pl.mateuszmarcyk.charity_donation_app.registration;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pl.mateuszmarcyk.charity_donation_app.event.RegistrationCompleteEvent;
import pl.mateuszmarcyk.charity_donation_app.event.ResendTokenEvent;
import pl.mateuszmarcyk.charity_donation_app.user.User;
import pl.mateuszmarcyk.charity_donation_app.user.UserService;

@RequiredArgsConstructor
@Service
public class RegistrationService {

    private final UserService userService;
    private final ApplicationEventPublisher publisher;

    @Value("${password.errorMessage}")
    private String errorMessage;

    public String getPasswordErrorIfExists(String password, String passwordRepeat) {
        if (passwordRepeat != null && passwordRepeat.equals(password)) {
            errorMessage = null;
        }
        return errorMessage;
    }

    public String getApplicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }


    public void registerUser(User user, HttpServletRequest request) {

        User savedUser = userService.save(user);

        publisher.publishEvent(new RegistrationCompleteEvent(savedUser, getApplicationUrl(request)));
    }

    public void resendToken(String oldToken, HttpServletRequest request) {

        User user = userService.findByVerificationToken(oldToken);
        String applicationUrl = getApplicationUrl(request);

        publisher.publishEvent(new ResendTokenEvent(user, applicationUrl, user.getVerificationToken()));
    }
}
