package pl.mateuszmarcyk.charity_donation_app.exception.exceptionhandler;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.mateuszmarcyk.charity_donation_app.exception.BusinessException;
import pl.mateuszmarcyk.charity_donation_app.exception.ResourceNotFoundException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenAlreadyConsumedException;
import pl.mateuszmarcyk.charity_donation_app.exception.TokenNotFoundException;

@ControllerAdvice
public class AppExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class,
            TokenNotFoundException.class,
            TokenAlreadyConsumedException.class
    })
    public String handleException(BusinessException exception, Model model) {

        model.addAttribute("errorMessage", exception.getMessage());
        model.addAttribute("errorTitle", exception.getTitle());

        return "error-page";
    }

}
