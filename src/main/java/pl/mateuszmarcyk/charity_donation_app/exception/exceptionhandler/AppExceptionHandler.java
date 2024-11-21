package pl.mateuszmarcyk.charity_donation_app.exception.exceptionhandler;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.mateuszmarcyk.charity_donation_app.exception.*;

@ControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class,
            TokenNotFoundException.class,
            TokenAlreadyConsumedException.class,
            TokenAlreadyExpiredException.class
    })
    public String handleException(BusinessException exception, Model model) {

        model.addAttribute("errorMessage", exception.getMessage());
        model.addAttribute("errorTitle", exception.getTitle());

        if (exception instanceof TokenAlreadyExpiredException tokenAlreadyExpiredException) {
            String token = tokenAlreadyExpiredException.getToken();
            model.addAttribute("tokenExpired", "tokenExpired");
            model.addAttribute("token", token);
        }

        return "error-page";
    }

}
