package pl.mateuszmarcyk.charity_donation_app.registration;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@AllArgsConstructor
@Service
public class RegistrationService {

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

}
