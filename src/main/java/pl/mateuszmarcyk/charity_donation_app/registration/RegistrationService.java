package pl.mateuszmarcyk.charity_donation_app.registration;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class RegistrationService {

    public String getPasswordErrorIfExists(String password, String passwordRepeat) {
        String errorMessage = "Given passwords are different";;
        if (passwordRepeat != null && passwordRepeat.equals(password)) {
            errorMessage = null;
        }
        return errorMessage;
    }

    public String getApplicationUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }

}
