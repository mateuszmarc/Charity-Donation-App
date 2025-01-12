package pl.mateuszmarcyk.charity_donation_app.validation;

import org.springframework.ui.Model;
import pl.mateuszmarcyk.charity_donation_app.config.security.CustomUserDetails;
import pl.mateuszmarcyk.charity_donation_app.entity.User;
import pl.mateuszmarcyk.charity_donation_app.util.LoggedUserModelHandler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class GlobalTestMethodVerifier {

    public static void verifyInvocationOfLoggedUserModelHandlerMethods(LoggedUserModelHandler loggedUserModelHandler) {
        verify(loggedUserModelHandler, times(1)).addUserToModel(any(User.class), any(Model.class));
        verify(loggedUserModelHandler, times(1)).getUser(any(CustomUserDetails.class));
    }
}
